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
    private int daysTillLaunch;

    //Constructors
    @ConstructorForDeserialization
    public BoardingTicket(String description, Party marsExpress, Party owner, int daysTillLaunch) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = owner;
        this.daysTillLaunch = daysTillLaunch;
    }

    public BoardingTicket(String description, Party marsExpress, int daysTillLaunch) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = marsExpress;
        this.daysTillLaunch = daysTillLaunch;
    }

    //Getters
    public String getDescription() {return description;}
    public Party getMarsExpress() {return marsExpress;}
    public Party getOwner() {return owner;}
    public int getDaysTillLaunch() {return daysTillLaunch;}

    //helper method
    public BoardingTicket changeOwner(Party owner){
        BoardingTicket newOwnerState = new BoardingTicket(this.description,this.marsExpress,owner,this.daysTillLaunch);
        return newOwnerState;
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "description : " + this.description +
                " marsExpress : " + this.marsExpress.getName().toString() +
                " owner : " + this.owner.getName().toString() +
                " daysTillLaunch : " + Integer.toString(this.daysTillLaunch);
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(marsExpress, owner);
    }

}
