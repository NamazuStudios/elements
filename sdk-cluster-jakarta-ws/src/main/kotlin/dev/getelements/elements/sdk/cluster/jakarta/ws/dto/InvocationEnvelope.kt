package dev.getelements.elements.sdk.cluster.jakarta.ws.dto

import dev.getelements.elements.sdk.cluster.remote.dto.Invocation

data class InvocationEnvelope(override val payload: Invocation) : Envelope<Invocation> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION

}
