package net.corda.solarsystem.flows;

import net.corda.solarsystem.message.ProbeMessageDto;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.InitiatingFlow;
import net.corda.v5.application.flows.JsonConstructor;
import net.corda.v5.application.flows.RpcStartFlowRequestParameters;
import net.corda.v5.application.flows.StartableByRPC;
import net.corda.v5.application.flows.flowservices.FlowIdentity;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.application.services.json.JsonMarshallingService;
import net.corda.v5.application.services.persistence.PersistenceService;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.base.stream.Cursor;

import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@InitiatingFlow
@StartableByRPC
public class ListProbeMessagesFlowJava implements Flow<List<ProbeMessageDto>> {

    @JsonConstructor
    public ListProbeMessagesFlowJava(RpcStartFlowRequestParameters params) {}

    @CordaInject
    private PersistenceService persistenceService;

    @CordaInject
    private JsonMarshallingService jsonMarshallingService;

    @CordaInject
    private FlowIdentity flowIdentity;

    @Override
    @Suspendable
    public List<ProbeMessageDto> call() {
        // Query for all persistent probe messages, and use the PersistentProbePostProcessor to convert the entities to serializable DTOs.
        Cursor<ProbeMessageDto> cursor = persistenceService.query(
                "ProbeSchemaV1Java.PersistentProbeMessageJava.FindAll",
                Collections.emptyMap(),
                "PersistentProbePostProcessor"
        );

        // poll for all ProbeMessages in batches of 100 with a 10 second timeout.
        Cursor.PollResult<ProbeMessageDto> poll = cursor.poll(100, Duration.ofSeconds(10));
        List<ProbeMessageDto> accumulator = new ArrayList<ProbeMessageDto>(poll.getValues());

        while (!poll.isLastResult()) {
            poll = cursor.poll(100, Duration.ofSeconds(10));
            accumulator.addAll(poll.getValues());
        }

        return accumulator;
    }
}
