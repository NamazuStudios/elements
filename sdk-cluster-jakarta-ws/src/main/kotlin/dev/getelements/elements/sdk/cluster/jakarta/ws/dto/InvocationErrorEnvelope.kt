package dev.getelements.elements.sdk.cluster.jakarta.ws.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError

/**
 * Houses the invocation error type.
 *
 * @property payload the invocation error
 */
data class InvocationErrorEnvelope(override val payload: InvocationError) : Envelope<InvocationError> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION_ERROR

}
