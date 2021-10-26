package net.corda.missionMars.contracts;

import net.corda.missionMars.states.MarsVoucher;
import net.corda.missionMars.states.TemplateState;
import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.v5.ledger.contracts.ContractsDSL.requireThat;

public class MarsVoucherContract implements Contract {
    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.missionMars.contracts.MarsVoucherContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        final CommandData commandData = tx.getCommands().get(0).getValue();
        if (commandData instanceof MarsVoucherContract.Commands.Issue) {
            //Retrieve the output state of the transaction
            MarsVoucher output = tx.outputsOfType(MarsVoucher.class).get(0);

            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("This transaction should only have one MarsVoucher state as output", tx.getOutputs().size() == 1);
                require.using("The output MarsVoucher state should have clear description of the type of Space trip information", !(output.getVoucherDesc().equals("")));
                return null;
            });
        }
    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class Issue implements MarsVoucherContract.Commands {}
    }
}
