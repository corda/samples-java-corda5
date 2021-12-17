package net.corda.c5template.contracts;

import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;

public class CartContract implements Contract {

    /**
     * The verify() function of all the states' contracts must not throw an exception for a transaction to be
     * considered valid.
     */
    @Override
    public void verify(LedgerTransaction tx) {
    }

    /**
     * This contracts only implements one command, Create.
     */
    public interface Commands extends CommandData {
        class Create implements Commands {}
    }
}