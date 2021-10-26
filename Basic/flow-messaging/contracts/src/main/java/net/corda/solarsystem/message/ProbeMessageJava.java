package net.corda.solarsystem.message;

import com.google.gson.Gson;
import net.corda.v5.application.identity.Party;
import net.corda.v5.application.utilities.JsonRepresentable;
import net.corda.v5.base.annotations.CordaSerializable;
import org.jetbrains.annotations.NotNull;
import java.util.UUID;

@CordaSerializable
public class ProbeMessageJava implements JsonRepresentable {

    private UUID linearId;
    private String message;
    //  Parties Involved
    private Party launcher;
    private Party target;

    public ProbeMessageJava(UUID linearId, String message, Party launcher, Party target) {
        this.linearId = linearId;
        this.message = message;
        this.launcher = launcher;
        this.target = target;
    }

    @NotNull
    @Override
    public String toJsonString() {
        return new Gson().toJson(toDto());
    }

    private ProbeMessageDto toDto() {
        return new ProbeMessageDto(
                message,
                launcher.getName().toString(),
                target.getName().toString(),
                linearId.toString()
        );
    }

    public UUID getLinearId() {
        return linearId;
    }

    public String getMessage() {
        return message;
    }

    public Party getLauncher() {
        return launcher;
    }

    public Party getTarget() {
        return target;
    }
}
