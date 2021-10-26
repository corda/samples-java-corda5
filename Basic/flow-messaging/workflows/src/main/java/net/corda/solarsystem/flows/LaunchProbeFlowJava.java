package net.corda.solarsystem.flows;

import net.corda.solarsystem.message.ProbeMessageJava;
import net.corda.solarsystem.messaging.ProbeMessageSendStatus;
import net.corda.v5.application.flows.BadRpcStartFlowRequestException;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.FlowSession;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.flows.JsonConstructor;
import net.corda.v5.application.flows.RpcStartFlowRequestParameters;
import net.corda.v5.application.flows.StartableByRPC;
import net.corda.v5.application.flows.UntrustworthyData;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.flows.flowservices.FlowMessaging;
import net.corda.v5.application.identity.CordaX500Name;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.IdentityService;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.NoSuchElementException;
import java.util.UUID;

@InitiatingFlow
@StartableByRPC
public class LaunchProbeFlowJava implements Flow<ProbeMessageJava> {

    private static final Logger logger = LoggerFactory.getLogger(LaunchProbeFlowAcceptorJava.class);

    private final RpcStartFlowRequestParameters params;

    @CordaInject
    private FlowIdentity flowIdentity;

    @CordaInject
    private FlowMessaging flowMessaging;

    @CordaInject
    private IdentityService identityService;

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @JsonConstructor
    public LaunchProbeFlowJava(RpcStartFlowRequestParameters params) {
        this.params = params;
    }

    @Override
    @Suspendable
    public ProbeMessageJava call() {
        // parse parameters
        Map<String, String> mapOfParams = jsonMarshallingService.parseJson(params.getParametersInJson(), Map.class);

        String message = mapOfParams.get("message");
        if (message == null) throw new BadRpcStartFlowRequestException("Parameter \"message\" missing.");

        String targetStr = mapOfParams.get("target");
        if (targetStr == null) throw new BadRpcStartFlowRequestException("Parameter \"target\" missing.");
        CordaX500Name target = CordaX500Name.parse(targetStr);

        Party recipientParty = identityService.partyFromName(target);
        if (recipientParty == null) throw new NoSuchElementException("No party found for X500 name $target");

        // Stage 1.
        // Generate a message.
        ProbeMessageJava probeMessage = new ProbeMessageJava(UUID.randomUUID(), message, flowIdentity.getOurIdentity(), recipientParty);

        // Stage 2.
        // Initiate a communication session with the counterparty.
        FlowSession flowSession = flowMessaging.initiateFlow(recipientParty);

        logger.info("Sending probe message '" + probeMessage.getMessage() + "' to '" + probeMessage.getTarget().getName() + "'");

        // Stage 3.
        // Send the probe message to the counterparty and await result.
        UntrustworthyData<ProbeMessageSendStatus> untrustworthyResponse = flowSession.sendAndReceive(ProbeMessageSendStatus.class, probeMessage);

        boolean success = untrustworthyResponse.unwrap(it -> it == ProbeMessageSendStatus.SUCCESS);
        if (!success) throw new CordaRuntimeException("Response from acceptor was not a success.");

        logger.info("Sent probe message with ID '" + probeMessage.getLinearId() + "' and received success response.");

        return probeMessage;
    }

    public FlowIdentity getFlowIdentity() {
        return flowIdentity;
    }

    public FlowMessaging getFlowMessaging() {
        return flowMessaging;
    }

    public IdentityService getIdentityService() {
        return identityService;
    }

    public JsonMarshallingService getJsonMarshallingService() {
        return jsonMarshallingService;
    }
}

