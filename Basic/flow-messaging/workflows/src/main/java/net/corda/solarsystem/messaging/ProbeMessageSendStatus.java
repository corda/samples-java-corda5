package net.corda.solarsystem.messaging;

import net.corda.v5.base.annotations.CordaSerializable;

@CordaSerializable
public enum ProbeMessageSendStatus {
    SUCCESS,
    FAILURE
}
