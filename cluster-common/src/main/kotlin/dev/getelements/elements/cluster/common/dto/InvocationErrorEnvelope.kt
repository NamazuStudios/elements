package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError

/**
 * Houses the invocation error type.
 *
 * @property payload the invocation error
 */
data class InvocationErrorEnvelope(val id: String, override val payload: InvocationError) : Envelope<InvocationError> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION_ERROR

}
