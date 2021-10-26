package net.corda.missionMars.contracts;

import net.corda.missionMars.states.BoardingTicket;
import net.corda.missionMars.states.MarsVoucher;
import net.corda.v5.ledger.contracts.CommandData;
import net.corda.v5.ledger.contracts.Contract;
import net.corda.v5.ledger.transactions.LedgerTransaction;
import org.jetbrains.annotations.NotNull;

import static net.corda.v5.ledger.contracts.ContractsDSL.requireThat;

public class BoardingTicketContract implements Contract {

    // This is used to identify our contract when building a transaction.
    public static final String ID = "net.corda.missionMars.contracts.BoardingTicketContract";

    @Override
    public void verify(@NotNull LedgerTransaction tx) {
        final CommandData commandData = tx.getCommands().get(0).getValue();
        BoardingTicket output = tx.outputsOfType(BoardingTicket.class).get(0);

        if (commandData instanceof BoardingTicketContract.Commands.CreateTicket) {
            //Using Corda DSL function requireThat to replicate conditions-checks
            requireThat(require -> {
                require.using("This transaction should only output one BoardingTicket state", tx.getOutputs().size() == 1);
                require.using("The output BoardingTicket state should have clear description of space trip information", !(output.getDescription().equals("")));
                require.using("The output BoardingTicket state should have a launching date later then the creation time", (output.getDaysTillLaunch() > 0));
                return null;
            });
        }else if(commandData instanceof BoardingTicketContract.Commands.RedeemTicket) {
            MarsVoucher input = tx.inputsOfType(MarsVoucher.class).get(0);
            requireThat(require -> {
                require.using("This transaction should consume two states", tx.getInputStates().size() == 2);
                require.using("The issuer of the BoardingTicket should be the space company which creates the boarding ticket", input.getIssuer().equals(output.getMarsExpress()));
                require.using("The output BoardingTicket state should have a launching date later then the creation time", (output.getDaysTillLaunch() > 0));
                return null;
            });
        }

    }

    // Used to indicate the transaction's intent.
    public interface Commands extends CommandData {
        //In our hello-world app, We will only have one command.
        class CreateTicket implements BoardingTicketContract.Commands {}
        class RedeemTicket implements BoardingTicketContract.Commands {}
    }
}
