package net.corda.c5template.schema;

import net.corda.v5.persistence.MappedSchema;
import org.jetbrains.annotations.Nullable;

import java.util.Arrays;

public class CartSchemaV1 extends MappedSchema {

    public CartSchemaV1() {
        super(CartSchema.class, 1, Arrays.asList(PersistentCart.class, PersistentUser.class));
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "cart.changelog-master";
    }
}
