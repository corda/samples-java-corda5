package net.corda.solarsystem.message;

import net.corda.v5.base.annotations.CordaSerializable;

/**
 * A JSON-serializable DTO for returning results from named-queries.
 */
@CordaSerializable
public class ProbeMessageDto {
    private final String message;
    private final String launcher;
    private final String target;
    private final String linearId;

    public ProbeMessageDto(String message, String launcher, String target, String linearId) {
        this.message = message;
        this.launcher = launcher;
        this.target = target;
        this.linearId = linearId;
    }

    public String getMessage() {
        return message;
    }

    public String getLauncher() {
        return launcher;
    }

    public String getTarget() {
        return target;
    }

    public String getLinearId() {
        return linearId;
    }
}

