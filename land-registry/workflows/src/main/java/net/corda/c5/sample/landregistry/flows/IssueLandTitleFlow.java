package net.corda.c5.sample.landregistry.flows;

import net.corda.c5.sample.landregistry.contracts.LandTitleContract;
import net.corda.c5.sample.landregistry.states.LandTitleState;
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
import net.corda.v5.ledger.services.NotaryLookupService;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

@InitiatingFlow
@StartableByRPC
public class IssueLandTitleFlow implements Flow<SignedTransactionDigest> {
    private RpcStartFlowRequestParameters params;

    @JsonConstructor
    public IssueLandTitleFlow(RpcStartFlowRequestParameters params) {
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

        Party notary = notaryLookupService.getNotaryIdentities().get(0);

        // Build the output state from the parameters passed to the flow
        LandTitleState landTitleState = getOutputState();

        // Build the transaction.
        TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                .setNotary(notary)
                .addOutputState(landTitleState)
                .addCommand(new LandTitleContract.Commands.Issue(), Arrays.asList(landTitleState.getIssuer().getOwningKey()));

        // Verify that the transaction is valid.
        transactionBuilder.verify();

        // Self Sign the transaction.
        SignedTransaction signedTx = transactionBuilder.sign();

        // Notarise and record the transaction in both parties' vaults
        SignedTransaction notarisedTx;
        if(landTitleState.getOwner().equals(landTitleState.getIssuer())){ // Self Issue
            notarisedTx = flowEngine.subFlow(new FinalityFlow(signedTx, Collections.emptyList()));
        }else {
            FlowSession receiverSession = flowMessaging.initiateFlow(landTitleState.getOwner());
            notarisedTx = flowEngine.subFlow(new FinalityFlow(signedTx, Arrays.asList(receiverSession)));
        }

        // Return Json output
        return new SignedTransactionDigest(notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                notarisedTx.getSigs());
    }

    private LandTitleState getOutputState(){
        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);
        checkParams(parametersMap);

        Party owner = identityService.partyFromName(CordaX500Name.parse(parametersMap.get("owner")));
        Party issuer = flowIdentity.getOurIdentity();
        String plotNumber = parametersMap.get("plotNumber");
        String dimensions = parametersMap.get("dimensions");
        String area = parametersMap.get("area");

        LandTitleState landTitleState = new LandTitleState(plotNumber, dimensions, area, owner, issuer);
        return landTitleState;
    }

    private void checkParams(Map<String, String> parametersMap){
        if(parametersMap.get("owner") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"owner\" ");
        if(parametersMap.get("plotNumber") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"plotNumber\" ");
        if(parametersMap.get("dimensions") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"dimensions\" ");
        if(parametersMap.get("area") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"area\" ");
    }
}




