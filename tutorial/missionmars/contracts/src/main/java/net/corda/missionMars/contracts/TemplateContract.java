package net.corda.missionMars.contracts;

import net.corda.missionMars.states.TemplateState;
import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.v5.ledger.contracts.ContractsDSL.requireThat;

// ************
// * Contract *
// ************
public class TemplateContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.missionMars.contracts.TemplateContract";

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        final CommandData commandData = tx.getCommands().get(0).getValue();

        if (commandData instanceof Commands.Send) {
            //Retrieve the output state of the transaction
            TemplateState output = tx.outputsOfType(TemplateState.class).get(0);

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("No inputs should be consumed when sending the Hello-World message.", tx.getInputStates().size() == 0);
                require.using("The message must be Hello-World", output.getMsg().equals("Hello-World"));
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Send implements Commands {}
    }
}
