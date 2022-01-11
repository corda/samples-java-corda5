package net.corda.c5template.flows;

import net.corda.c5template.contracts.CartContract;
import net.corda.c5template.states.CartState;
import net.corda.c5template.states.User;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.*;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.flows.flowservices.FlowMessaging;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.IdentityService;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.services.NotaryLookupService;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.NoSuchElementException;

//this flow is called by seller when he adds items to the cart
@InitiatingFlow
@StartableByRPC
public class CreateCartFlow implements Flow<SignedTransactionDigest> {
    private RpcStartFlowRequestParameters params;

    @JsonConstructor
    public CreateCartFlow(RpcStartFlowRequestParameters params) {
        this.params = params;
    }

    @CordaInject
    private FlowEngine flowEngine;

    @CordaInject
    private FlowIdentity flowIdentity;

    @CordaInject
    private FlowMessaging flowMessaging;

    @CordaInject
    private TransactionBuilderFactory transactionBuilderFactory;

    @CordaInject
    private IdentityService identityService;

    @CordaInject
    private NotaryLookupService notaryLookupService;

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @Override
    @Suspendable
    public SignedTransactionDigest call() {
        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);
        String cartName;
        String userName;
        String userId;
        CordaX500Name buyer;

        if (!parametersMap.containsKey("cartName"))
            throw new BadRpcStartFlowRequestException("Parameter \"cartName\" missing.");
        else
            cartName = parametersMap.get("cartName");

        if (!parametersMap.containsKey("userName"))
            throw new BadRpcStartFlowRequestException("Parameter \"itemName\" missing.");
        else
            userName = parametersMap.get("userName");

        if (!parametersMap.containsKey("userId"))
            throw new BadRpcStartFlowRequestException("Parameter \"itemId\" missing.");
        else
            userId = parametersMap.get("userId");

        if (!parametersMap.containsKey("buyer"))
            throw new BadRpcStartFlowRequestException("Parameter \"buyer\" missing.");
        else
            buyer = CordaX500Name.parse(parametersMap.get("buyer"));

        Party buyerParty = identityService.partyFromName(buyer);
        if (buyerParty == null) throw new NoSuchElementException("No party found for X500 name " +buyerParty);

        Party notary = notaryLookupService.getNotaryIdentities().get(0);

        Party sellerParty = flowIdentity.getOurIdentity();

        CartState cartState = new CartState(cartName,
                new User(userId, userName), buyerParty, sellerParty);

        final Command<CartContract.Commands.Create> txCommand = new Command<>(
                new CartContract.Commands.Create(),
                Arrays.asList(cartState.getBuyer().getOwningKey(), cartState.getSeller().getOwningKey()));

        TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(cartState)
                .addCommand(txCommand);

        transactionBuilder.verify();

        SignedTransaction signedTransaction = transactionBuilder.sign();

        FlowSession buyerSession = flowMessaging.initiateFlow(buyerParty);

        SignedTransaction fullySignedTx = flowEngine.subFlow(new CollectSignaturesFlow(signedTransaction,
                Arrays.asList(buyerSession)));

        SignedTransaction notarisedTx = flowEngine.subFlow(new FinalityFlow(fullySignedTx,
                Arrays.asList(buyerSession)));

        return new SignedTransactionDigest(notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.
                        getTx().getOutputStates().get(0))),
                notarisedTx.getSigs());
    }
}
