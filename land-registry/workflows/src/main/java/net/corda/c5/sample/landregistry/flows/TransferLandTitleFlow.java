package net.corda.c5.sample.landregistry.flows;

import net.corda.c5.sample.landregistry.contracts.LandTitleContract;
import net.corda.c5.sample.landregistry.states.LandTitleState;
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
import net.corda.v5.application.services.persistence.PersistenceService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.stream.Cursor;
import net.corda.v5.ledger.contracts.StateAndRef;
import net.corda.v5.ledger.services.NotaryLookupService;
import net.corda.v5.ledger.services.vault.SetBasedVaultQueryFilter;
import net.corda.v5.ledger.services.vault.StateStatus;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;

import java.time.Duration;
import java.util.*;

@InitiatingFlow
@StartableByRPC
public class TransferLandTitleFlow implements Flow<SignedTransactionDigest> {
    private RpcStartFlowRequestParameters params;

    @JsonConstructor
    public TransferLandTitleFlow(RpcStartFlowRequestParameters params) {
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

    @CordaInject
    private PersistenceService persistenceService;

    @Override
    @Suspendable
    public SignedTransactionDigest call() {

        Party notary = notaryLookupService.getNotaryIdentities().get(0);

        Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);
        if(parametersMap.get("plotNumber") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"plotNumber\" ");

        if(parametersMap.get("owner") == null)
            throw new BadRpcStartFlowRequestException("Missing Parameter: \"owner\" ");

        String plotNumber = parametersMap.get("plotNumber");
        Party owner = identityService.partyFromName(CordaX500Name.parse(parametersMap.get("owner")));


        //Query the landState
        Map <String, Object> namedParameters = new LinkedHashMap<>();
        namedParameters.put("stateStatus", StateStatus.UNCONSUMED);
        Cursor<StateAndRef<LandTitleState>> cursor = persistenceService.query(
                "VaultState.findByStateStatus",
                namedParameters,
                new SetBasedVaultQueryFilter.Builder()
                        .withContractStateClassNames(Set.of(LandTitleState.class.getName()))
                        .build(),
                "Corda.IdentityStateAndRefPostProcessor"
        );

        List<StateAndRef<LandTitleState>> inputLandStateStateAndRefList =
                cursor.poll(100, Duration.ofSeconds(20)).getValues();

        StateAndRef<LandTitleState> inputLandStateStateAndRef =
                inputLandStateStateAndRefList.stream().filter(stateAndRef -> {
            LandTitleState landTitleState = stateAndRef.getState().getData();
            return landTitleState.getPlotNumber().equals(plotNumber);
        }).findAny().orElseThrow(() -> new FlowException("Land Not Found"));

        LandTitleState outputLandTitleState = getOutputState(inputLandStateStateAndRef, owner);

        // Build the transaction.
        TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                .setNotary(inputLandStateStateAndRef.getState().getNotary())
                .addInputState(inputLandStateStateAndRef)
                .addOutputState(outputLandTitleState)
                .addCommand(new LandTitleContract.Commands.Transfer(),
                        Arrays.asList(outputLandTitleState.getOwner().getOwningKey(),
                                inputLandStateStateAndRef.getState().getData().getIssuer().getOwningKey(),
                                inputLandStateStateAndRef.getState().getData().getOwner().getOwningKey()));

        // Verify that the transaction is valid.
        transactionBuilder.verify();

        // Self Sign the transaction.
        SignedTransaction selfSignedTx = transactionBuilder.sign();

        // Send the state to the counterparty, and receive their signature.
        FlowSession ownerSession = flowMessaging.initiateFlow(outputLandTitleState.getOwner());
        FlowSession issuerSession = flowMessaging.initiateFlow(inputLandStateStateAndRef.getState().getData().getIssuer());
        SignedTransaction fullySignedTx = flowEngine.subFlow(new CollectSignaturesFlow(selfSignedTx,
                Arrays.asList(ownerSession, issuerSession)));

        // Notarise and record the transaction. Add issuer session so that they get a copy of the transaction.
        SignedTransaction notarisedTx;
            notarisedTx = flowEngine.subFlow(new FinalityFlow(fullySignedTx, Arrays.asList(ownerSession, issuerSession)));

        // Return Json output
        return new SignedTransactionDigest(notarisedTx.getId(),
                Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                notarisedTx.getSigs());
    }

    private LandTitleState getOutputState(StateAndRef<LandTitleState> inputStateAndRef, Party owner){
        LandTitleState inputState = inputStateAndRef.getState().getData();
        LandTitleState landTitleState = new LandTitleState(inputState.getPlotNumber(), inputState.getDimensions(),
                inputState.getArea(), owner, inputState.getIssuer());
        return landTitleState;
    }
}




