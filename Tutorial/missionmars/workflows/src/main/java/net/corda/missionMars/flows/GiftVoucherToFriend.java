package net.corda.missionMars.flows;

import net.corda.missionMars.contracts.BoardingTicketContract;
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
import net.corda.v5.application.identity.AbstractParty;
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
import net.corda.v5.ledger.services.vault.StateStatus;
import net.corda.v5.ledger.transactions.SignedTransaction;
import net.corda.v5.ledger.transactions.SignedTransactionDigest;
import net.corda.v5.ledger.transactions.TransactionBuilder;
import net.corda.v5.ledger.transactions.TransactionBuilderFactory;
import net.corda.v5.legacyapi.flows.FlowLogic;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class GiftVoucherToFriend {

    @InitiatingFlow
    @StartableByRPC
    public static class GiftVoucherToFriendInitiator implements Flow<SignedTransactionDigest> {

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
        @CordaInject
        private PersistenceService persistenceService;
        //Private variable
        private RpcStartFlowRequestParameters params;

        //Constructor
        @JsonConstructor
        public GiftVoucherToFriendInitiator(RpcStartFlowRequestParameters params) {
            this.params = params;
        }

        @Override
        @Suspendable
        public SignedTransactionDigest call() {

            //Retrieve JSON params
            Map<String, String> parametersMap = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

            //Retrieve State parameter fields from JSON
            //Voucher ID
            String voucherID;
            if(!parametersMap.containsKey("voucherID"))
                throw new BadRpcStartFlowRequestException("MarsVoucher State Parameter \"voucherID\" missing.");
            else
                voucherID = parametersMap.get("voucherID");
            //RecipientParty
            CordaX500Name holder;
            if(!parametersMap.containsKey("holder"))
                throw new BadRpcStartFlowRequestException("MarsVoucher State Parameter \"holder\" missing.");
            else
                holder = CordaX500Name.parse(parametersMap.get("holder"));
            Party recipientParty;
            recipientParty = identityService.partyFromName(holder);

            //Query the MarsVoucher & the boardingTicket
            Map <String, Object> namedParameters = new LinkedHashMap<String,Object>();
            namedParameters.put("uuid", UUID.fromString(voucherID));
            namedParameters.put("stateStatus", StateStatus.UNCONSUMED);
            Cursor cursor = persistenceService.query(
                    "LinearState.findByUuidAndStateStatus",
                    namedParameters,
                    "Corda.IdentityStateAndRefPostProcessor"
            );
            StateAndRef<MarsVoucher> marsVoucherStateAndRef = (StateAndRef<MarsVoucher>) cursor.poll(100, Duration.ofSeconds(20)).getValues().get(0);
            MarsVoucher inputMarsVoucher = marsVoucherStateAndRef.getState().getData();

            //Check if the initiator is indeed the holder of the mars voucher
            if(!(inputMarsVoucher.getHolder().getOwningKey().equals(flowIdentity.getOurIdentity().getOwningKey())))
                throw new FlowException("Only the voucher current holder can initiate a gifting transaction");

            //Building the output
            MarsVoucher outputMarsVoucher = inputMarsVoucher.changeOwner(recipientParty);

            //Get the Notary from inputRef
            Party notary = marsVoucherStateAndRef.getState().getNotary();

            TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                    .setNotary(notary)
                    .addInputState(marsVoucherStateAndRef)
                    .addOutputState(outputMarsVoucher, MarsVoucherContract.ID)
                    .addCommand(new MarsVoucherContract.Commands.Transfer(),
                            Arrays.asList(recipientParty.getOwningKey(),
                                    inputMarsVoucher.getHolder().getOwningKey(),
                                    inputMarsVoucher.getIssuer().getOwningKey()));

            // Verify that the transaction is valid.
            transactionBuilder.verify();

            // Sign the transaction.
            SignedTransaction partialSignedTx = transactionBuilder.sign();

            // Send the state to the counterparty, and receive it back with their signature.
            List<FlowSession> receiverSession = new ArrayList<>();

            for (AbstractParty participant: inputMarsVoucher.getParticipants()) {
                Party partyToInitiateFlow = (Party) participant;
                if (!partyToInitiateFlow.getOwningKey().equals(flowIdentity.getOurIdentity().getOwningKey())) {
                    receiverSession.add(flowMessaging.initiateFlow(partyToInitiateFlow));
                }
            }
            receiverSession.add(flowMessaging.initiateFlow(recipientParty));

            SignedTransaction fullySignedTx = flowEngine.subFlow(
                    new CollectSignaturesFlow(partialSignedTx, receiverSession));

            // Notarise and record the transaction in both parties' vaults
            SignedTransaction notarisedTx = flowEngine.subFlow(
                    new FinalityFlow(fullySignedTx, receiverSession));

            // Return Json output
            return new SignedTransactionDigest(notarisedTx.getId(),
                    Collections.singletonList(jsonMarshallingService.formatJson(notarisedTx.getTx().getOutputStates().get(0))),
                    notarisedTx.getSigs());
        }
    }


    @InitiatedBy(GiftVoucherToFriendInitiator.class)
    public static class GiftVoucherToFriendResponder implements Flow<SignedTransaction> {

        //Node Injectables
        @CordaInject
        private FlowEngine flowEngine;

        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public GiftVoucherToFriendResponder(FlowSession counterpartySession) {
            this.counterpartySession = counterpartySession;
        }

        @Suspendable
        @Override
        public SignedTransaction call() throws FlowException {
            SignedTransaction signedTransaction = flowEngine.subFlow(
                    new CreateAndIssueMarsVoucher.CreateAndIssueMarsVoucherResponder.signingTransaction(counterpartySession));
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
