package net.corda.c5.sample.landregistry.flows;

import net.corda.systemflows.ReceiveFinalityFlow;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.FlowException;
import net.corda.v5.application.flows.FlowSession;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.transactions.SignedTransaction;

@InitiatedBy(IssueLandTitleFlow.class)
public class IssueLandTitleFlowResponder implements Flow<SignedTransaction> {

    @CordaInject
    private FlowEngine flowEngine;

    private FlowSession counterpartySession;

    public IssueLandTitleFlowResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }

    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        return flowEngine.subFlow(new ReceiveFinalityFlow(counterpartySession));
    }

}
