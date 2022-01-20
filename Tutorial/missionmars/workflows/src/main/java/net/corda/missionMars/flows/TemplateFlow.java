package net.corda.missionMars.flows;

import net.corda.missionMars.contracts.TemplateContract;
import net.corda.missionMars.states.TemplateState;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.*;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.flows.flowservices.FlowMessaging;
import net.corda.v5.application.identity.AbstractParty;
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
import java.util.stream.Collectors;

@InitiatingFlow
@StartableByRPC
public class TemplateFlow implements Flow<SignedTransactionDigest> {
    private RpcStartFlowRequestParameters params;

    @JsonConstructor
    public TemplateFlow(RpcStartFlowRequestParameters params) {
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

        Party notary = notaryLookupService.getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"));

        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

        Party sender = flowIdentity.getOurIdentity();
        String msg;
        CordaX500Name receiverName;
        Party receiver;
        if(!parametersMap.containsKey("msg"))
            throw new BadRpcStartFlowRequestException("Template State Parameter \"msg\" missing.");
        else
            msg = parametersMap.get("msg");

        if(!parametersMap.containsKey("receiver"))
            throw new BadRpcStartFlowRequestException("Template State Parameter \"receiver\" missing.");
        else
            receiverName = CordaX500Name.parse(parametersMap.get("receiver"));

        receiver = identityService.partyFromName(receiverName);

        TemplateState templateState = new TemplateState(msg, sender, receiver);

        // Stage 1.
        // Generate an unsigned transaction.
        Command txCommand = new Command(new TemplateContract.Commands.Send(), Arrays.asList(sender.getOwningKey(), receiver.getOwningKey()));
        TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(templateState,TemplateContract.ID)
                .addCommand(txCommand);

        // Stage 2.
        // Verify that the transaction is valid.
        transactionBuilder.verify();

        // Stage 3.
        // Sign the transaction.
        SignedTransaction partialSignedTx = transactionBuilder.sign();

        // Stage 4.
        // Send the state to the counterparty, and receive it back with their signature.
        FlowSession receiverSession = flowMessaging.initiateFlow(receiver);

        SignedTransaction fullySignedTx = flowEngine.subFlow(new CollectSignaturesFlow(partialSignedTx,
                    Arrays.asList(receiverSession)));

        // Stage 5.
        // Notarise and record the transaction in both parties' vaults
        SignedTransaction notarisedTx = flowEngine.subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(receiverSession)));

        //Step 6.
        // Return Json output
        return new SignedTransactionDigest(notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                notarisedTx.getSigs());
    }

    public FlowEngine getFlowEngine() {
        return flowEngine;
    }
    public NotaryLookupService getNotaryLookup() {
        return this.notaryLookupService;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public JsonMarshallingService getJsonMarshallingService() {
        return jsonMarshallingService;
    }
}




