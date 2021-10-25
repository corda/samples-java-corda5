package net.corda.c5.sample.landregistry.contracts;

import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

// ************
// * Contract *
// ************
public class LandTitleContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.c5.sample.landregistry.LandContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        // Implement your contract validation logic here.
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Issue implements Commands {}
        class Transfer implements Commands {}
    }
}
