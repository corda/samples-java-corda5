package net.corda.c5template.schema;

import net.corda.v5.ledger.schemas.PersistentState;

import javax.persistence.*;
import java.io.Serializable;

@Entity
@Table(name = "cart")
//@NamedQueries({
//        @NamedQuery(
//                name="PersistentCart.findCartByCartName",
//                query="FROM net.corda.c5template.schema.PersistentCart c WHERE c.name = :cartName"
//        ),
//        @NamedQuery(name = "PersistentCart.findCartByUser",
//                query="FROM net.corda.c5template.schema.PersistentCart c WHERE c.user = :user"
//        )
//})
@NamedQuery(
        name="PersistentCart.findCartByCartName",
        query="FROM net.corda.c5template.schema.PersistentCart c WHERE c.name = :cartName"
)
public class PersistentCart extends PersistentState implements Serializable {

    @Column
    private String name;

    @OneToOne(cascade = CascadeType.PERSIST)
    @JoinColumns({
            @JoinColumn(name = "userId"),
    })
    private PersistentUser user;

    // Default constructor required by Hibernate
    public PersistentCart() {
        this.name = null;
        this.user = null;
    }

    public PersistentCart(String name, PersistentUser user) {
        this.name = name;
        this.user = user;
    }

    public String getName() {
        return name;
    }

    public PersistentUser getUser() {
        return user;
    }
}