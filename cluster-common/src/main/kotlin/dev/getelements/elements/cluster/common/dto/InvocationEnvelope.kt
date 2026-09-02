package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.Invocation

data class InvocationEnvelope(val id: String, override val payload: Invocation) : Envelope<Invocation> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION

}
