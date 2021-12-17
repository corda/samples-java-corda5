package net.corda.c5template.schema;

import net.corda.v5.ledger.schemas.PersistentState;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;

import javax.persistence.*;
import java.io.Serializable;
import java.util.List;

@Entity
@Table(name = "cart")
public class PersistentCart extends PersistentState implements Serializable {

    @Column
    private String name;

    @OneToMany(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "output_index", referencedColumnName = "output_index"),
            @JoinColumn(name = "transaction_id", referencedColumnName = "transaction_id"),
    })
    private List<PersistentItem> items;

    // Default constructor required by Hibernate
    public PersistentCart() {
        this.name = null;
        this.items = null;
    }

    @ConstructorForDeserialization
    public PersistentCart(String name, List<PersistentItem> items) {
        this.name = name;
        this.items = items;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<PersistentItem> getItems() {
        return items;
    }

    public void setItems(List<PersistentItem> items) {
        this.items = items;
    }
}