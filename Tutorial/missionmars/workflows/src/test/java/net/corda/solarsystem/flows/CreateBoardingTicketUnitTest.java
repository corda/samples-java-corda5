package net.corda.solarsystem.flows;

import net.corda.missionMars.contracts.BoardingTicketContract;
import net.corda.missionMars.flows.CreateBoardingTicket;
import net.corda.missionMars.states.BoardingTicket;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.RpcStartFlowRequestParameters;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.ledger.contracts.Command;
import net.corda.v5.ledger.contracts.CommandData;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.corda.testing.flow.utils.FlowMockUtils.flowTest;

public class CreateBoardingTicketUnitTest {

    @Test
    public void flowSignsStateTest() {
        flowTest(CreateBoardingTicket.CreateBoardingTicketInitiator.class, flowMockHelper -> {
            // NOTE: this probably should be set up in flowTest
            CordaX500Name MockNode = CordaX500Name.parse("O=MockNode, L=London, C=GB, OU=Template");

            //Input parameters that helps trigger the flow (The flow will NOT actually use this input in test)
            String inputParams = "{\"msg\": \"Hello-World\", \"receiver\": \"${mockNode}\"}";

            //Start the mock flow
            flowMockHelper.createFlow( fmh -> new CreateBoardingTicket.CreateBoardingTicketInitiator(new RpcStartFlowRequestParameters(inputParams)));

            //Give the notary to the test
            Mockito.doReturn(flowMockHelper.getNotary())
                    .when(flowMockHelper.getFlow().getNotaryLookup())
                    .getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"));

            //Ask Testhelper to sign the transaction accordingly
            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(CollectSignaturesFlow.class));

            //Ask Testhelper to finalize the transaction
            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(FinalityFlow.class));

            //Give Testhelper to the output
            LocalDate launchDay = LocalDate.parse("2023-11-02");
            List<BoardingTicket> outputs = List.of(new BoardingTicket("Space Shuttle 323 - 16B", flowMockHelper.getOurIdentity(), flowMockHelper.getOtherSide(),launchDay));
            Mockito.doReturn(flowMockHelper.getWireTransactionMock())
                    .when(flowMockHelper.getSignedTransactionMock())
                    .getTx();
            Mockito.doReturn(outputs)
                    .when(flowMockHelper.getWireTransactionMock())
                    .getOutputStates();

            //Give the input JSON to the flow
            HashMap<String, String> inputMap = new HashMap();
            inputMap.put("ticketDescription", "Space Shuttle 323 - 16B");
            inputMap.put("launchDate", "2023-11-02");
            Mockito.doReturn(inputMap)
                    .when(flowMockHelper.getFlow().getJsonMarshallingService())
                    .parseJson(inputParams, Map.class);

            //Run the flow
            flowMockHelper.getFlow().call();

            // verify notary is set
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).setNotary(flowMockHelper.getNotary());

            // verify the correct output state is created
            ArgumentCaptor<BoardingTicket> stateArgCaptor = ArgumentCaptor.forClass(BoardingTicket.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addOutputState(stateArgCaptor.capture(), Mockito.eq(BoardingTicketContract.ID));
            Assertions.assertThat(stateArgCaptor.getValue().getMarsExpress()).isEqualTo(flowMockHelper.getOurIdentity());
            Assertions.assertThat(stateArgCaptor.getValue().getOwner()).isEqualTo(flowMockHelper.getOurIdentity());
            Assertions.assertThat(stateArgCaptor.getValue().getDescription()).isEqualTo("Space Shuttle 323 - 16B");
            Assertions.assertThat(stateArgCaptor.getValue().getlaunchDate()).isEqualTo(launchDay);


            // verify command is added
            ArgumentCaptor<Command<CommandData>> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addCommand(commandArgumentCaptor.capture());
            Assertions.assertThat(commandArgumentCaptor.getValue().getValue()).isInstanceOf(BoardingTicketContract.Commands.CreateTicket.class);
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOurIdentity().getOwningKey());
        });
    }
}
