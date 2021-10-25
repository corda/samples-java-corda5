package net.corda.c5.sample.landregistry.states;

import net.corda.c5.sample.landregistry.contracts.LandTitleContract;
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
@BelongsToContract(LandTitleContract.class)
public class LandTitleState implements ContractState, JsonRepresentable {

    /* This is the unique identifier of the property */
    private String plotNumber;
    private String dimensions;
    private String area;

    private Party owner;
    private Party issuer;

    /* Constructor of our Corda state */

    public LandTitleState(String plotNumber, String dimensions, String area, Party owner, Party issuer) {
        this.plotNumber = plotNumber;
        this.dimensions = dimensions;
        this.area = area;
        this.owner = owner;
        this.issuer = issuer;
    }

    //Getters
    public String getPlotNumber() {
        return plotNumber;
    }

    public Party getOwner() {
        return owner;
    }

    public Party getIssuer() {
        return issuer;
    }

    public String getDimensions() {
        return dimensions;
    }

    public String getArea() {
        return area;
    }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(issuer, owner);
    }

    @NotNull
    @Override
    public String toJsonString() {
        return  "plotNumber: " + plotNumber +
                " dimensions: " + dimensions +
                " area: " + area +
                " owner: " + owner +
                " issuer: " + issuer;
    }
}