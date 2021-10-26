package net.corda.solarsystem.schema;

import net.corda.v5.base.annotations.CordaSerializable;
import net.corda.v5.persistence.MappedSchema;
import net.corda.v5.serialization.annotations.ConstructorForDeserialization;
import org.jetbrains.annotations.Nullable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import java.util.List;

/**
 * A probe message schema.
 */
public class ProbeSchemaV1Java extends MappedSchema {
    public ProbeSchemaV1Java() {
        super(ProbeSchemaJava.class, 1, List.of(PersistentProbeMessageJava.class));
    }

    @Nullable
    @Override
    public String getMigrationResource() {
        return "probe-java.changelog-master";
    }

    @Entity
    @NamedQuery(
            name = "ProbeSchemaV1Java.PersistentProbeMessageJava.FindAll",
            query = "FROM net.corda.solarsystem.schema.ProbeSchemaV1Java$PersistentProbeMessageJava"
    )
    @Table(name = "probe_message_java")
    @CordaSerializable
    public static class PersistentProbeMessageJava {
        @Id
        @Column(name = "linear_id")
        String linearId;

        @Column(name = "message")
        String message;

        @Column(name = "launcher")
        String launcherName;

        @Column(name = "target")
        String targetName;

        public PersistentProbeMessageJava() {
        }

        @ConstructorForDeserialization
        public PersistentProbeMessageJava(String linearId, String message, String launcherName, String targetName) {
            this.linearId = linearId;
            this.message = message;
            this.launcherName = launcherName;
            this.targetName = targetName;
        }

        private void setLinearId(String linearId) {
            this.linearId = linearId;
        }

        private void setMessage(String message) {
            this.message = message;
        }

        private void setLauncherName(String launcherName) {
            this.launcherName = launcherName;
        }

        private void setTargetName(String targetName) {
            this.targetName = targetName;
        }

        public String getLinearId() {
            return linearId;
        }

        public String getMessage() {
            return message;
        }

        public String getLauncherName() {
            return launcherName;
        }

        public String getTargetName() {
            return targetName;
        }
    }
}
