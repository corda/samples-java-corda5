package net.corda.missionMars.states;

import net.corda.missionMars.contracts.BoardingTicketContract;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.contracts.ContractState;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
@BelongsToContract(BoardingTicketContract.class)
public class BoardingTicket implements ContractState, JsonRepresentable {

    //Private Variables
    private String description;
    private Party marsExpress;
    private Party owner;
    private int daysUntilLaunch;

    //Constructors
    @ConstructorForDeserialization
    public BoardingTicket(String description, Party marsExpress, Party owner, int daysUntilLaunch) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = owner;
        this.daysUntilLaunch = daysUntilLaunch;
    }

    public BoardingTicket(String description, Party marsExpress, int daysUntilLaunch) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = marsExpress;
        this.daysUntilLaunch = daysUntilLaunch;
    }

    //Getters
    public String getDescription() {return description;}
    public Party getMarsExpress() {return marsExpress;}
    public Party getOwner() {return owner;}
    public int getdaysUntilLaunch() {return daysUntilLaunch;}

    //helper method
    public BoardingTicket changeOwner(Party owner){
        BoardingTicket newOwnerState = new BoardingTicket(this.description,this.marsExpress,owner,this.daysUntilLaunch);
        return newOwnerState;
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "description : " + this.description +
                " marsExpress : " + this.marsExpress.getName().toString() +
                " owner : " + this.owner.getName().toString() +
                " daysUntilLaunch : " + Integer.toString(this.daysUntilLaunch);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(marsExpress, owner);
    }

}
