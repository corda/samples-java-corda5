package net.corda.c5template.flows;

import net.corda.systemflows.ReceiveFinalityFlow;
import net.corda.systemflows.SignTransactionFlow;
import net.corda.v5.application.flows.Flow;
import net.corda.v5.application.flows.FlowException;
import net.corda.v5.application.flows.FlowSession;
import net.corda.v5.application.flows.InitiatedBy;
import net.corda.v5.application.flows.flowservices.FlowEngine;
import net.corda.v5.application.injection.CordaInject;
import net.corda.v5.base.annotations.Suspendable;
import net.corda.v5.ledger.transactions.SignedTransaction;
import org.jetbrains.annotations.NotNull;

@InitiatedBy(CreateCartFlow.class)
public class CreateCartFlowResponder implements Flow<SignedTransaction> {
    @CordaInject
    private FlowEngine flowEngine;
    private FlowSession counterpartySession;
    public CreateCartFlowResponder(FlowSession counterpartySession) {
        this.counterpartySession = counterpartySession;
    }
    @Suspendable
    @Override
    public SignedTransaction call() throws FlowException {
        SignedTransaction signedTransaction = flowEngine.subFlow(new MySignTransactionFlow(counterpartySession));

        return flowEngine.subFlow(new ReceiveFinalityFlow(counterpartySession, signedTransaction.getId()));
    }
    public static class MySignTransactionFlow extends SignTransactionFlow {
        MySignTransactionFlow(FlowSession counterpartySession) {
            super(counterpartySession);
        }
        @Override
        protected void checkTransaction(@NotNull SignedTransaction stx) {
        }
    }
}
