package net.corda.solarsystem.postprocessors;

import net.corda.solarsystem.message.ProbeMessageDto;
import net.corda.solarsystem.schema.ProbeSchemaV1Java;
import net.corda.v5.application.services.persistence.CustomQueryPostProcessor;
import org.jetbrains.annotations.NotNull;

import java.util.stream.Stream;

public class PersistentProbePostProcessor implements CustomQueryPostProcessor<ProbeMessageDto> {
    @NotNull
    @Override
    public String getName() {
        return "PersistentProbePostProcessor";
    }

    @NotNull
    @Override
    public Stream<ProbeMessageDto> postProcess(@NotNull Stream<Object> inputs) {
        return inputs
                .filter(it -> it instanceof ProbeSchemaV1Java.PersistentProbeMessageJava)
                .map(it -> {
                    ProbeSchemaV1Java.PersistentProbeMessageJava cast = (ProbeSchemaV1Java.PersistentProbeMessageJava) it;
                    return new ProbeMessageDto(cast.getLinearId(), cast.getMessage(), cast.getLauncherName(), cast.getTargetName());
                });
    }
}
