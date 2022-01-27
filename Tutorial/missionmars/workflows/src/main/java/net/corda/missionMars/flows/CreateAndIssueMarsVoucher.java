package net.corda.missionMars.flows;

import net.corda.missionMars.contracts.MarsVoucherContract;
import net.corda.missionMars.states.MarsVoucher;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.systemflows.ReceiveFinalityFlow;
import net.corda.systemflows.SignTransactionFlow;
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
import net.corda.v5.ledger.UniqueIdentifier;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.services.NotaryLookupService;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class CreateAndIssueMarsVoucher {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateAndIssueMarsVoucherInitiator implements Flow<SignedTransactionDigest> {

        //Node Injectables
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

        //Private variable
        private RpcStartFlowRequestParameters params;

        //Constructor
        @JsonConstructor
        public CreateAndIssueMarsVoucherInitiator(RpcStartFlowRequestParameters params) {
            this.params = params;
        }

        @Override
        @Suspendable
        public SignedTransactionDigest call() {

            //Getting Notary
            Party notary = notaryLookupService.getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"));

            //Retrieve JSON params
            Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

            //Retrieve State parameter fields from JSON
            Party issuer = flowIdentity.getOurIdentity();
            String voucherDesc;
            if(!parametersMap.containsKey("voucherDesc"))
                throw new BadRpcStartFlowRequestException("MarsVoucher State Parameter \"voucherDesc\" missing.");
            else
                voucherDesc = parametersMap.get("voucherDesc");
            CordaX500Name target;
            if(!parametersMap.containsKey("holder"))
                throw new BadRpcStartFlowRequestException("MarsVoucher State Parameter \"holder\" missing.");
            else
                target = CordaX500Name.parse(parametersMap.get("holder"));
            Party holder;
            holder = identityService.partyFromName(target);

            //Building the output MarsVoucher state
            UniqueIdentifier uniqueID = new UniqueIdentifier();
            MarsVoucher newVoucher = new MarsVoucher(voucherDesc,issuer,holder,uniqueID);
            Command txCommand = new Command(new MarsVoucherContract.Commands.Issue(), Arrays.asList(issuer.getOwningKey(), holder.getOwningKey()));

            //Build transaction
            TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                    .setNotary(notary)
                    .addOutputState(newVoucher,MarsVoucherContract.ID)
                    .addCommand(txCommand);

            // Verify that the transaction is valid.
            transactionBuilder.verify();

            // Sign the transaction.
            SignedTransaction partialSignedTx = transactionBuilder.sign();

            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession receiverSession = flowMessaging.initiateFlow(holder);

            SignedTransaction fullySignedTx = flowEngine.subFlow(
                    new CollectSignaturesFlow(partialSignedTx, Arrays.asList(receiverSession)));

            // Notarise and record the transaction in both parties' vaults
            SignedTransaction notarisedTx = flowEngine.subFlow(
                    new FinalityFlow(fullySignedTx, Arrays.asList(receiverSession)));

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


    @InitiatedBy(CreateAndIssueMarsVoucherInitiator.class)
    public static class CreateAndIssueMarsVoucherResponder implements Flow<SignedTransaction> {

        //Node Injectables
        @CordaInject
        private FlowEngine flowEngine;

        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public CreateAndIssueMarsVoucherResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignedTransaction signedTransaction = flowEngine.subFlow(
                    new CreateAndIssueMarsVoucherResponder.signingTransaction(counterpartySession));
            return flowEngine.subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
        }

        public static class signingTransaction extends SignTransactionFlow {
            signingTransaction(FlowSession counterpartySession) {
                super(counterpartySession);
            }
            @Override
            protected void checkTransaction(@NotNull SignedTransaction stx) {
            }
        }
    }
}
