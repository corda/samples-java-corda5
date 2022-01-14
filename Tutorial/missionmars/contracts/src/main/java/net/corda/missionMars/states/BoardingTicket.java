package net.corda.missionMars.states;

import net.corda.missionMars.contracts.BoardingTicketContract;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.contracts.ContractState;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
@BelongsToContract(BoardingTicketContract.class)
public class BoardingTicket implements ContractState, JsonRepresentable {

    //Private Variables
    private String description;
    private Party marsExpress;
    private Party owner;
    private LocalDate launchDate;

    //Constructors
    @ConstructorForDeserialization
    public BoardingTicket(String description, Party marsExpress, Party owner, LocalDate launchDate) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = owner;
        this.launchDate = launchDate;
    }

    public BoardingTicket(String description, Party marsExpress, LocalDate launchDate) {
        this.description = description;
        this.marsExpress = marsExpress;
        this.owner = marsExpress;
        this.launchDate = launchDate;
    }

    //Getters
    public String getDescription() {return description;}
    public Party getMarsExpress() {return marsExpress;}
    public Party getOwner() {return owner;}
    public LocalDate getlaunchDate() {return launchDate;}

    //helper method
    public BoardingTicket changeOwner(Party owner){
        BoardingTicket newOwnerState = new BoardingTicket(this.description,this.marsExpress,owner,this.launchDate);
        return newOwnerState;
    }

    @NotNull
    @Override
    public String toJsonString() {
        return "description : " + this.description +
                " marsExpress : " + this.marsExpress.getName().toString() +
                " owner : " + this.owner.getName().toString() +
                " launchDate : " + this.launchDate.toString();
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(marsExpress, owner);
    }

}
