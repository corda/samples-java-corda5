package net.corda.missionMars.flows;

import net.corda.missionMars.contracts.BoardingTicketContract;
import net.corda.missionMars.contracts.MarsVoucherContract;
import net.corda.missionMars.states.BoardingTicket;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.*;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
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
import net.corda.v5.legacyapi.flows.FlowLogic;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;

public class CreateBoardingTicket {

    @InitiatingFlow
    @StartableByRPC
    public static class CreateBoardingTicketInitiator implements Flow<SignedTransactionDigest> {

        //Node Injectables
        @CordaInject
        private FlowEngine flowEngine;
        @CordaInject
        private FlowIdentity flowIdentity;
        @CordaInject
        private TransactionBuilderFactory transactionBuilderFactory;
        @CordaInject
        private NotaryLookupService notaryLookupService;
        @CordaInject
        private JsonMarshallingService jsonMarshallingService;

        //Private variable
        private RpcStartFlowRequestParameters params;

        //Constructor
        @JsonConstructor
        public CreateBoardingTicketInitiator(RpcStartFlowRequestParameters params) {
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
            String ticketDescription;
            if(!parametersMap.containsKey("ticketDescription"))
                throw new BadRpcStartFlowRequestException("BoardingTicket State Parameter \"ticketDescription\" missing.");
            else
                ticketDescription = parametersMap.get("ticketDescription");

            LocalDate launchDate;
            if(!parametersMap.containsKey("launchDate"))
                throw new BadRpcStartFlowRequestException("BoardingTicket State Parameter \"launchDate\" missing.");
            else
                launchDate = LocalDate.parse(parametersMap.get("launchDate"));
          
            //Building the output MarsVoucher state
            Party marsExpress = flowIdentity.getOurIdentity();
            BoardingTicket ticket = new BoardingTicket(ticketDescription,marsExpress,launchDate);
            Command txCommand = new Command(new BoardingTicketContract.Commands.CreateTicket(), Arrays.asList(marsExpress.getOwningKey()));

            //Build transaction
            TransactionBuilder transactionBuilder = transactionBuilderFactory.create()
                    .setNotary(notary)
                    .addOutputState(ticket, BoardingTicketContract.ID)
                    .addCommand(txCommand);


            // Verify that the transaction is valid.
            transactionBuilder.verify();

            // Sign the transaction.
            SignedTransaction partialSignedTx = transactionBuilder.sign();

            // Notarise and record the transaction in both parties' vaults
            SignedTransaction notarisedTx = flowEngine.subFlow(
                    new FinalityFlow(partialSignedTx, Collections.emptyList()));

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

        public JsonMarshallingService getJsonMarshallingService() {
            return jsonMarshallingService;
        }
    }

}
