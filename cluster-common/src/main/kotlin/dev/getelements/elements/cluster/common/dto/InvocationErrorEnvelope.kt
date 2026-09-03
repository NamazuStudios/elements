package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError

/**
 * Wraps the failure of a remote [dev.getelements.elements.sdk.cluster.remote.dto.Invocation], returned to
 * the caller identified by [id] in place of an [InvocationResultEnvelope] when the invocation could not
 * complete successfully.
 *
 * @property id the unique identifier correlating this error with its originating [InvocationEnvelope]
 * @property payload the invocation error
 */
data class InvocationErrorEnvelope(val id: String, override val payload: InvocationError) : Envelope<InvocationError> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION_ERROR

}
