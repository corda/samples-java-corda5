package net.corda.c5template.flows;

import net.corda.c5template.states.CartState;
import net.corda.v5.application.flows.*;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.application.services.persistence.PersistenceService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.stream.Cursor;
import net.corda.v5.ledger.services.vault.IdentityContractStatePostProcessor;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

//this flow is called by seller when he adds items to the cart
@InitiatingFlow
@StartableByRPC
public class FindAllItemsByCartName implements Flow<List<String>> {
    private RpcStartFlowRequestParameters params;

    @JsonConstructor
    public FindAllItemsByCartName(RpcStartFlowRequestParameters params) {
        this.params = params;
    }

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @CordaInject
    private PersistenceService persistenceService;

    @Override
    @Suspendable
    public List<String> call() {
        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

        String cartName;

        if (!parametersMap.containsKey("cartName"))
            throw new BadRpcStartFlowRequestException("Parameter \"cartName\" missing.");
        else
            cartName = parametersMap.get("cartId");

        Map<String, Object> namedParameters = new LinkedHashMap<>();
        namedParameters.put("cartName", cartName);

        Cursor<CartState> cursor = persistenceService.query(
                "PersistentCart.findCartByCartName",
                namedParameters,
                IdentityContractStatePostProcessor.POST_PROCESSOR_NAME
        );

        ArrayList<CartState> accumulator = new ArrayList<>();
        Cursor.PollResult<CartState> poll;
        do {
            poll = cursor.poll(100, Duration.of(10, ChronoUnit.SECONDS));
            accumulator.addAll(poll.getValues());
        } while (!poll.isLastResult());

        System.out.println("Size of result : " + accumulator.size());
        System.out.println("Name: " + accumulator.get(0) != null ?accumulator.get(0).getName() : 0);
        System.out.println("User details: " + accumulator.get(0) != null ?(accumulator.get(0).getUser() !=null ? accumulator.get(0).getUser().getName() : 0) : 0);

        return accumulator.stream()
                .map(message -> jsonMarshallingService.formatJson("helloooo - "))
                .collect(Collectors.toList());
    }
}
