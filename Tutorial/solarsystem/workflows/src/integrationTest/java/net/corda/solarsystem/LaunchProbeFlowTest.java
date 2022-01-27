package net.corda.solarsystem;

import com.google.gson.GsonBuilder;
import kong.unirest.*;
import kong.unirest.json.JSONObject;
import kotlin.jvm.functions.Function1;
import net.corda.solarsystem.flows.LaunchProbeFlowJava;
import net.corda.test.dev.network.Credentials;
import net.corda.test.dev.network.Node;
import net.corda.test.dev.network.Nodes;
import net.corda.test.dev.network.TestNetwork;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInstance;

import java.time.Duration;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class LaunchProbeFlowTest {

    private TestNetwork testNetwork;

    @BeforeAll
    public void setup() {
        testNetwork = TestNetwork.Companion.forNetwork("solar-system");
    }

    @Test
    public void startLaunchProbeFlow() {
        testNetwork.use((Function1<Nodes<Node>, Void>) o -> {
            Node pluto = o.getNode("pluto");
            Node earth = o.getNode("earth");
            try {
                startLaunchProbeFlow(earth, "earthling", "password", "Hello pluto", "C=US, L=NINTH, O=PLUTO, OU=DWARF_PLANET", "false");
            } catch (InterruptedException e) {
                fail("Test failed with interrupted exception");
            }

            return null;
        });
    }

    private void startLaunchProbeFlow(Node planet,
                                      String username,
                                      String password,
                                      String message,
                                      String target,
                                      String planetaryOnly) throws InterruptedException {
        planet.httpRpc(
                new Credentials(username, password),
                Unirest.config(),
                "api",
                "1",
                (UnirestInstance u) -> {
                    String clientId = "client-" + UUID.randomUUID();

                    String parametersInJson = new GsonBuilder().create().toJson(
                            Map.of(
                                    "message", message,
                                    "target", target,
                                    "planetaryOnly", planetaryOnly
                            )
                    );
                    Map<String, Object> body = Map.of(
                            "rpcStartFlowRequest", Map.of(
                                    "flowName", LaunchProbeFlowJava.class.getName(),
                                    "clientId", clientId,
                                    "parameters", Map.of("parametersInJson", parametersInJson)
                            )
                    );
                    RequestBodyEntity request = u.post("flowstarter/startflow")
                            .header("Content-Type", "application/json")
                            .body(body);

                    HttpResponse<JsonNode> response = request.asJson();

                    assertEquals(HttpStatus.SC_OK, response.getStatus());
                    assertEquals(clientId, response.getBody().getObject().get("clientId"));
                    JSONObject flowId = (JSONObject) response.getBody().getObject().get("flowId");
                    assertNotNull(flowId);
                    String uuid = (String) flowId.get("uuid");

                    try {
                        eventually(Duration.ofSeconds(5), Duration.ofMillis(100), Duration.ofMillis(100), () -> {
                            HttpResponse<JsonNode> outcomeResponse = retrieveOutcome(uuid);
                            assertEquals(HttpStatus.SC_OK, outcomeResponse.getStatus());
                            assertEquals("COMPLETED", outcomeResponse.getBody().getObject().get("status"));
                            return true;
                        });
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