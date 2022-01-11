package net.corda.c5template.schema;

import net.corda.v5.base.annotations.CordaSerializable;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;

@Entity
@Table(name = "user")
@CordaSerializable
public class PersistentUser implements Serializable {

    @Id
    @Column
    private String userId;
    @Column
    private String name;

    public PersistentUser(String userId, String name) {
        this.userId = userId;
        this.name = name;
    }

    public PersistentUser() {
    }

    public String getUserId() {
        return userId;
    }

    public String getName() {
        return name;
    }
}
