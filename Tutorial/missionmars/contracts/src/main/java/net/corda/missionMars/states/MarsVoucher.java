package net.corda.missionMars.states;

import net.corda.missionMars.contracts.MarsVoucherContract;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.ledger.UniqueIdentifier;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.contracts.LinearState;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;
@BelongsToContract(MarsVoucherContract.class)
public class MarsVoucher implements LinearState, JsonRepresentable {

    private String voucherDesc;
    private Party issuer;
    private Party holder;
    private UniqueIdentifier linearId;

    /* Constructor of your Corda state */
    public MarsVoucher(String voucherDesc, Party issuer, Party holder, UniqueIdentifier linearId) {
        this.voucherDesc = voucherDesc;
        this.issuer = issuer;
        this.holder = holder;
        this.linearId = linearId;
    }

    //getters
    public String getVoucherDesc() { return voucherDesc; }
    public Party getIssuer() { return issuer; }
    public Party getHolder() { return holder; }

    @NotNull
    @Override
    public String toJsonString() {
        return "voucherDesc : " + this.voucherDesc +
                " issuer : " + this.issuer.getName().toString() +
                " holder : " + this.holder.getName().toString() +
                " linearId: " + this.linearId.toString();
    }

    /* This method will indicate who are the participants and required signers when
     * this state is used in a transaction. */
    @NotNull
    @Override
    public List<AbstractParty> getParticipants() { return Arrays.asList(this.issuer, this.holder); }

    @NotNull
    @Override
    public UniqueIdentifier getLinearId() {
        return this.linearId;
    }
}
