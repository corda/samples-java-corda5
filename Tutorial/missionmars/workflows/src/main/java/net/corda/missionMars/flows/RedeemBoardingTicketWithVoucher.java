package net.corda.missionMars.flows;

import net.corda.missionMars.contracts.BoardingTicketContract;
import net.corda.missionMars.states.BoardingTicket;
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
import net.corda.v5.legacyapi.flows.FlowLogic;
import org.jetbrains.annotations.NotNull;

import java.time.Duration;
import java.util.*;

public class RedeemBoardingTicketWithVoucher {

    @InitiatingFlow
    @StartableByRPC
    public static class RedeemBoardingTicketWithVoucherInitiator implements Flow<SignedTransactionDigest> {

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
        public RedeemBoardingTicketWithVoucherInitiator(RpcStartFlowRequestParameters params) {
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
                throw new BadRpcStartFlowRequestException("BoardingTicket State Parameter \"holder\" missing.");
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

            Map <String, Object> namedParameters2 = new LinkedHashMap<String,Object>();
            namedParameters2.put("stateStatus", StateStatus.UNCONSUMED);
            Set<String> ContractStateClassName = new LinkedHashSet<>();
            ContractStateClassName.add(BoardingTicket.class.getName());

            Cursor cursor2 = persistenceService.query(
                    "VaultState.findByStateStatus",
                    namedParameters2,
                    new SetBasedVaultQueryFilter.Builder()
                            .withContractStateClassNames(ContractStateClassName)
                            .build(),
                    "Corda.IdentityStateAndRefPostProcessor"
            );
            StateAndRef<BoardingTicket> boardingTicketStateAndRef = (StateAndRef<BoardingTicket>) cursor2.poll(100, Duration.ofSeconds(20)).getValues().get(0);
            BoardingTicket originalBoardingTicketState= boardingTicketStateAndRef.getState().getData();

            //Building the output
            BoardingTicket outputBoardingTicket = originalBoardingTicketState.changeOwner(recipientParty);

            //Getting Notary
            Party notary = boardingTicketStateAndRef.getState().getNotary();

            //Build transaction
            TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                    .setNotary(notary)
                    .addInputState(marsVoucherStateAndRef)
                    .addInputState(boardingTicketStateAndRef)
                    .addOutputState(outputBoardingTicket, BoardingTicketContract.ID)
                    .addCommand(new BoardingTicketContract.Commands.RedeemTicket(),
                            Arrays.asList(recipientParty.getOwningKey(),flowIdentity.getOurIdentity().getOwningKey()));

            // Verify that the transaction is valid.
            transactionBuilder.verify();

            // Sign the transaction.
            SignedTransaction partialSignedTx = transactionBuilder.sign();

            // Send the state to the counterparty, and receive it back with their signature.
            FlowSession receiverSession = flowMessaging.initiateFlow(recipientParty);

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
    }


    @InitiatedBy(RedeemBoardingTicketWithVoucherInitiator.class)
    public static class RedeemBoardingTicketWithVoucherResponder implements Flow<SignedTransaction> {

        //Node Injectables
        @CordaInject
        private FlowEngine flowEngine;

        //private variable
        private FlowSession counterpartySession;

        //Constructor
        public RedeemBoardingTicketWithVoucherResponder(FlowSession counterpartySession) {
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
