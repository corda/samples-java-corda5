package net.corda.c5template.states;

import net.corda.c5template.contracts.CartContract;
import net.corda.c5template.schema.CartSchemaV1;
import net.corda.c5template.schema.PersistentCart;
import net.corda.c5template.schema.PersistentUser;
import net.corda.v5.application.identity.AbstractParty;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.ledger.contracts.BelongsToContract;
import net.corda.v5.ledger.schemas.PersistentState;
import net.corda.v5.ledger.schemas.QueryableState;
import net.corda.v5.persistence.MappedSchema;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

@BelongsToContract(CartContract.class)
@CordaSerializable
public class CartState implements QueryableState, JsonRepresentable {
    private String name;
    private User user;
    private Party buyer;
    private Party seller;

    @ConstructorForDeserialization
    public CartState(String name, User user, Party buyer, Party seller) {
        this.name = name;
        this.user = user;
        this.buyer = buyer;
        this.seller = seller;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public Party getBuyer() {
        return buyer;
    }

    public void setBuyer(Party buyer) {
        this.buyer = buyer;
    }

    public Party getSeller() {
        return seller;
    }

    public void setSeller(Party seller) {
        this.seller = seller;
    }

    @NotNull
    @Override
    public List<AbstractParty> getParticipants() {
        return Arrays.asList(buyer, seller);
    }

    @NotNull
    @Override
    public PersistentState generateMappedObject(@NotNull MappedSchema schema) {
        if (schema instanceof CartSchemaV1) {
            return new PersistentCart(name, new PersistentUser(getUser().getUserId(), getUser().getName()));
        } else {
            throw new IllegalArgumentException("Unrecognised schema $schema");
        }
    }

    @NotNull
    @Override
    public Iterable<MappedSchema> supportedSchemas() {
        return Arrays.asList(new CartSchemaV1());
    }

    @NotNull
    @Override
    public String toJsonString() {
        return String.format("IOUState(name=%s, buyer=%s, seller=%s)",
                getName(), getBuyer(), getSeller());

    }
}
