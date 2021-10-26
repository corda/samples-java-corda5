package net.corda.solarsystem.flows;

import net.corda.solarsystem.message.ProbeMessageJava;
import net.corda.solarsystem.messaging.ProbeMessageSendStatus;
import net.corda.solarsystem.schema.ProbeSchemaV1Java;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.FlowSession;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.UntrustworthyData;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.persistence.PersistenceService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.exceptions.CordaRuntimeException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@InitiatedBy(LaunchProbeFlowJava.class)
public class LaunchProbeFlowAcceptorJava implements Flow<Void> {

    private static final Logger logger = LoggerFactory.getLogger(LaunchProbeFlowAcceptorJava.class);

    private final FlowSession counterPartySession;

    @CordaInject
    private FlowIdentity flowIdentity;

    @CordaInject
    private PersistenceService persistenceService;

    public LaunchProbeFlowAcceptorJava(FlowSession counterPartySession) {
        this.counterPartySession = counterPartySession;
    }

    @Override
    @Suspendable
    public Void call() {
        UntrustworthyData<ProbeMessageJava> untrustworthyData = counterPartySession.receive(ProbeMessageJava.class);

        logger.info("Received probe message, attempting to unwrap it.");

        // Request may not be trustworthy, make some assertions on the request that should be true for valid ProbeMessages.
        try {
            ProbeMessageJava probeMessage = untrustworthyData.unwrap (it -> {
                if (it.getMessage().isEmpty()) throw new CordaRuntimeException("FAILURE - request from counterparty did not contain a message.");
                if (!it.getTarget().getName().toString().equals(flowIdentity.getOurIdentity().getName().toString())) throw new CordaRuntimeException("FAILURE - request from counterparty did not have our identity as the target.");
                return it;
            });

            logger.info("Probe message '"+ probeMessage.getMessage() +"' from sender '"+probeMessage.getLauncher().getName() +"' successfully unwrapped.");

            // Record the message in our data store.
            persistenceService.persist(createPersistentProbeMessage(probeMessage));

            logger.info("Probe message with ID '"+ probeMessage.getLinearId().toString() +"' persisted into data storage.");

            // Send the success to the other party.
            counterPartySession.send(ProbeMessageSendStatus.SUCCESS);

        } catch (CordaRuntimeException e){
            logger.error("Exception during unwrapping of untrustworthy data from counterparty.", e);
            // Send the failure to the other party.
            counterPartySession.send(ProbeMessageSendStatus.FAILURE);
            return null;
        }
        return null;
    }

    private ProbeSchemaV1Java.PersistentProbeMessageJava createPersistentProbeMessage(ProbeMessageJava probeMessage) {
        return new ProbeSchemaV1Java.PersistentProbeMessageJava(
                probeMessage.getLinearId().toString(),
                probeMessage.getMessage(),
                probeMessage.getLauncher().getName().toString(),
                probeMessage.getTarget().getName().toString()
        );
    }

    public FlowIdentity getFlowIdentity() {
        return flowIdentity;
    }

    public PersistenceService getPersistenceService() {
        return persistenceService;
    }
}
