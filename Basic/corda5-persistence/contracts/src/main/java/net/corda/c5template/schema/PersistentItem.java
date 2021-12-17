package net.corda.c5template.schema;

import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "item")
@CordaSerializable
public class PersistentItem implements Serializable {
    @Id
    @Column
    private String id;
    @Column
    private String name;
    @Column
    private Integer cost;

    @ConstructorForDeserialization
    public PersistentItem(String id, String name, Integer cost) {
        this.id = id;
        this.name = name;
        this.cost = cost;
    }

    // Default constructor required by Hibernate
    public PersistentItem() {
        this.id = null;
        this.name = null;
        this.cost = null;
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Integer getCost() {
        return cost;
    }
}
