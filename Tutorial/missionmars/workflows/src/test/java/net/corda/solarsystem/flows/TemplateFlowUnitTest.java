package net.corda.solarsystem.flows;

import net.corda.missionMars.contracts.TemplateContract;
import net.corda.missionMars.flows.TemplateFlow;
import net.corda.missionMars.states.TemplateState;
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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static net.corda.testing.flow.utils.FlowMockUtils.flowTest;

public class TemplateFlowUnitTest {

    @Test
    public void flowSignsStateTest() {
        flowTest(TemplateFlow.class, flowMockHelper -> {
            // NOTE: this probably should be set up in flowTest
            CordaX500Name MockNode = CordaX500Name.parse("O=MockNode, L=London, C=GB, OU=Template");

            String inputParams = "{\"msg\": \"Hello-World\", \"receiver\": \"${mockNode}\"}";
            flowMockHelper.createFlow( fmh -> new TemplateFlow(new RpcStartFlowRequestParameters(inputParams)));

            Mockito.doReturn(flowMockHelper.getNotary())
                    .when(flowMockHelper.getFlow().getNotaryLookup())
                    .getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"));

            Mockito.doReturn(MockNode)
                    .when(flowMockHelper.getOtherSide())
                    .getName();
            Mockito.doReturn(flowMockHelper.getOtherSide())
                    .when(flowMockHelper.getFlow().getIdentityService())
                    .partyFromName(MockNode);

            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(CollectSignaturesFlow.class));

            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(FinalityFlow.class));

            List<TemplateState> outputs = List.of(new TemplateState("Hello-World", flowMockHelper.getOurIdentity(), flowMockHelper.getOtherSide()));
            Mockito.doReturn(flowMockHelper.getWireTransactionMock())
                    .when(flowMockHelper.getSignedTransactionMock())
                    .getTx();
            Mockito.doReturn(outputs)
                    .when(flowMockHelper.getWireTransactionMock())
                    .getOutputStates();

            HashMap<String, String> inputMap = new HashMap();
            inputMap.put("msg", "Hello-World");
            inputMap.put("receiver", flowMockHelper.getOtherSide().getName().toString());

            Mockito.doReturn(inputMap)
                    .when(flowMockHelper.getFlow().getJsonMarshallingService())
                    .parseJson(inputParams, Map.class);

            flowMockHelper.getFlow().call();

            // verify notary is set
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).setNotary(flowMockHelper.getNotary());

            // verify the correct output state is created
            ArgumentCaptor<TemplateState> stateArgCaptor = ArgumentCaptor.forClass(TemplateState.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addOutputState(stateArgCaptor.capture(), Mockito.eq(TemplateContract.ID));
            Assertions.assertThat(stateArgCaptor.getValue().getSender()).isEqualTo(flowMockHelper.getOurIdentity());
            Assertions.assertThat(stateArgCaptor.getValue().getReceiver()).isEqualTo(flowMockHelper.getOtherSide());
            Assertions.assertThat(stateArgCaptor.getValue().getMsg()).isEqualTo("Hello-World");

//            // verify command is added
            ArgumentCaptor<Command<CommandData>> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addCommand(commandArgumentCaptor.capture());
            Assertions.assertThat(commandArgumentCaptor.getValue().getValue()).isInstanceOf(TemplateContract.Commands.Send.class);
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOurIdentity().getOwningKey());
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOtherSide().getOwningKey());
        });
    }
}
