package net.corda.missionMars;

import com.google.gson.GsonBuilder;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.RequestBodyEntity;
import kong.unirest.Unirest;
import kong.unirest.UnirestInstance;
import kong.unirest.json.JSONObject;
import kotlin.jvm.functions.Function1;
import net.corda.missionMars.flows.CreateAndIssueMarsVoucher;
import net.corda.test.dev.network.Credentials;
import net.corda.test.dev.network.Node;
import net.corda.test.dev.network.Nodes;
import net.corda.test.dev.network.TestNetwork;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.fail;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class CreateAndIssueMarsVoucherTest {
    private TestNetwork testNetwork;

    @BeforeAll
    public void setup() {
        testNetwork = TestNetwork.Companion.forNetwork("missionmars-network");
    }

    @Test
    public void startTest() {
        testNetwork.use((Function1<Nodes<Node>, Void>) o -> {
            Node partyA = o.getNode("PartyA");
            Node partyB = o.getNode("PartyB");
            try {
                //"parametersInJson": "{\"voucherDesc\": \"Space Shuttle 323\", \"holder\": \"C=US, L=New York, O=Peter, OU=INC\"}"
                startCreateAndIssueFlow(partyA, "angelenos", "password", "Space Shuttle 323", "C=US, L=New York, O=Peter, OU=INC");
            } catch (InterruptedException e) {
                fail("Test failed with interrupted exception");
            }

            return null;
        });
    }

    private void startCreateAndIssueFlow(Node node,
                                      String username,
                                      String password,
                                      String voucherDesc,
                                      String holder) throws InterruptedException {
        node.httpRpc(
                new Credentials(username, password),
                Unirest.config(),
                "api",
                "1",
                (UnirestInstance u) -> {
                    String clientId = "client-" + UUID.randomUUID();

                    String parametersInJson = new GsonBuilder().create().toJson(
                            Map.of(
                                    "voucherDesc", voucherDesc,
                                    "holder", holder
                            )
                    );
                    Map<String, Object> body = Map.of(
                            "rpcStartFlowRequest", Map.of(
                                    "flowName", CreateAndIssueMarsVoucher.CreateAndIssueMarsVoucherInitiator.class.getName(),
                                    "clientId", clientId,
                                    "parameters", Map.of("parametersInJson", parametersInJson)
                            )
                    );
                    RequestBodyEntity request = u.post("flowstarter/startflow")
                            .header("Content-Type", "application/json")
                            .body(body);

                    HttpResponse<JsonNode> response = request.asJson();

                    assertEquals(org.apache.http.HttpStatus.SC_OK, response.getStatus());
                    assertEquals(clientId, response.getBody().getObject().get("clientId"));
                    JSONObject flowId = (JSONObject) response.getBody().getObject().get("flowId");
                    assertNotNull(flowId);
                    String uuid = (String) flowId.get("uuid");

                    try {
                        eventually(Duration.ofSeconds(5), Duration.ofMillis(100), Duration.ofMillis(100), () -> {
                            HttpResponse<JsonNode> outcomeResponse = retrieveOutcome(uuid);
                            assertEquals(org.apache.http.HttpStatus.SC_OK, outcomeResponse.getStatus());
                            assertEquals("COMPLETED", outcomeResponse.getBody().getObject().get("status"));
                            return true;
                        });
                        HttpResponse<JsonNode> outcomeResponse = retrieveOutcome(uuid);
                        String resultString = (String) outcomeResponse.getBody().getObject().get("resultJson");
                        System.out.println("--------------------------------");
                        System.out.println("Create and Issue Mars Voucher Result: ");
                        System.out.println("Flow ID: " + flowId);
                        System.out.println(resultString);
                        System.out.println("--------------------------------");

                    } catch (InterruptedException e) {
                        throw new AssertionError("Failed due to interrupted exception.");
                    }
                    return null;
                }
        );
    }

    private HttpResponse<JsonNode> retrieveOutcome(String flowId) {
        return Unirest.get("flowstarter/flowoutcome/" + flowId)
                .header("Content-Type", "application/json")
                .asJson();
    }

    private Boolean eventually(
            Duration duration,
            Duration waitBetween,
            Duration waitBefore,
            Supplier<Boolean> test
    ) throws InterruptedException {
        Long end = System.nanoTime() + duration.toNanos();
        Integer times = 0;
        AssertionError lastFailure = null;

        if (!waitBefore.isZero()) Thread.sleep(waitBefore.toMillis());

        while (System.nanoTime() < end) {
            try {
                return test.get();
            } catch (AssertionError e) {
                if (!waitBetween.isZero()) Thread.sleep(waitBetween.toMillis());
                lastFailure = e;
            }
            times++;
        }

        throw new AssertionError("Test failed with \"${lastFailure?.message}\" after $duration; attempted $times times");
    }
}
