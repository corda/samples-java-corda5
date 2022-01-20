package net.corda.solarsystem.flows;

import net.corda.missionMars.contracts.MarsVoucherContract;
import net.corda.missionMars.flows.CreateAndIssueMarsVoucher;
import net.corda.missionMars.states.MarsVoucher;
import net.corda.systemflows.CollectSignaturesFlow;
import net.corda.systemflows.FinalityFlow;
import net.corda.v5.application.flows.RpcStartFlowRequestParameters;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.ledger.UniqueIdentifier;
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

public class CreateAndIssueMarsVoucherUnitTest {

    @Test
    public void flowSignsStateTest() {
        flowTest(CreateAndIssueMarsVoucher.CreateAndIssueMarsVoucherInitiator.class, flowMockHelper -> {
            // NOTE: this probably should be set up in flowTest
            CordaX500Name MockNode = CordaX500Name.parse("O=MockNode, L=London, C=GB, OU=Template");

            //Input parameters that helps trigger the flow (The flow will NOT actually use this input in test)
            String inputParams = "{\"msg\": \"Hello-World\", \"receiver\": \"${mockNode}\"}";

            //Start the mock flow
            flowMockHelper.createFlow( fmh -> new CreateAndIssueMarsVoucher.CreateAndIssueMarsVoucherInitiator(new RpcStartFlowRequestParameters(inputParams)));

            //Give the notary to the test
            Mockito.doReturn(flowMockHelper.getNotary())
                    .when(flowMockHelper.getFlow().getNotaryLookup())
                    .getNotary(CordaX500Name.parse("O=notary, L=London, C=GB"));

            //Give the counter party to the Testhelper
            Mockito.doReturn(MockNode)
                    .when(flowMockHelper.getOtherSide())
                    .getName();
            Mockito.doReturn(flowMockHelper.getOtherSide())
                    .when(flowMockHelper.getFlow().getIdentityService())
                    .partyFromName(MockNode);

            //Ask Testhelper to sign the transaction accordingly
            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(CollectSignaturesFlow.class));

            //Ask Testhelper to finalize the transaction
            Mockito.doReturn(flowMockHelper.getSignedTransactionMock())
                    .when(flowMockHelper.getFlow().getFlowEngine())
                    .subFlow(Mockito.any(FinalityFlow.class));

            //Give Testhelper to the output
            UniqueIdentifier uniqueID = new UniqueIdentifier();
            List<MarsVoucher> outputs = List.of(new MarsVoucher("Space Shuttle 323", flowMockHelper.getOurIdentity(), flowMockHelper.getOtherSide(),uniqueID));
            Mockito.doReturn(flowMockHelper.getWireTransactionMock())
                    .when(flowMockHelper.getSignedTransactionMock())
                    .getTx();
            Mockito.doReturn(outputs)
                    .when(flowMockHelper.getWireTransactionMock())
                    .getOutputStates();

            //Give the input JSON to the flow
            HashMap<String, String> inputMap = new HashMap();
            inputMap.put("voucherDesc", "Space Shuttle 323");
            inputMap.put("holder", flowMockHelper.getOtherSide().getName().toString());
            Mockito.doReturn(inputMap)
                    .when(flowMockHelper.getFlow().getJsonMarshallingService())
                    .parseJson(inputParams, Map.class);

            //Run the flow
            flowMockHelper.getFlow().call();

            // verify notary is set
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).setNotary(flowMockHelper.getNotary());

            // verify the correct output state is created
            ArgumentCaptor<MarsVoucher> stateArgCaptor = ArgumentCaptor.forClass(MarsVoucher.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addOutputState(stateArgCaptor.capture(), Mockito.eq(MarsVoucherContract.ID));
            Assertions.assertThat(stateArgCaptor.getValue().getIssuer()).isEqualTo(flowMockHelper.getOurIdentity());
            Assertions.assertThat(stateArgCaptor.getValue().getHolder()).isEqualTo(flowMockHelper.getOtherSide());
            Assertions.assertThat(stateArgCaptor.getValue().getVoucherDesc()).isEqualTo("Space Shuttle 323");

            // verify command is added
            ArgumentCaptor<Command<CommandData>> commandArgumentCaptor = ArgumentCaptor.forClass(Command.class);
            Mockito.verify(flowMockHelper.getTransactionBuilderMock()).addCommand(commandArgumentCaptor.capture());
            Assertions.assertThat(commandArgumentCaptor.getValue().getValue()).isInstanceOf(MarsVoucherContract.Commands.Issue.class);
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOurIdentity().getOwningKey());
            Assertions.assertThat(commandArgumentCaptor.getValue().getSigners()).contains(flowMockHelper.getOtherSide().getOwningKey());
        });
    }
}
