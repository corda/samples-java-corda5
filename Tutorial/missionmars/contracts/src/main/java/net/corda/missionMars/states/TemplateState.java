package net.corda.missionMars.states;

import net.corda.missionMars.contracts.TemplateContract;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.contracts.ContractState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

// *********
// * State *
// *********
@BelongsToContract(TemplateContract.class)
public class TemplateState implements ContractState, JsonRepresentable {

    private String msg;
    private Party sender;
    private Party receiver;

    /* Constructor of your Corda state */
    public TemplateState(String msg, Party sender, Party receiver) {
        this.msg = msg;
        this.sender = sender;
        this.receiver = receiver;
    }

    //getters
    public String getMsg() {
        return msg;
    }

    public Party getSender() {
        return sender;
    }

    public Party getReceiver() {
        return receiver;
    }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(sender, receiver);
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "msg : " + msg + " sender : " + sender.getName().toString() + " receiver : " + receiver.getName().toString();
    }
}