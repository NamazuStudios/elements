package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
/**
 * Wraps an [Invocation] request sent from a caller to a remote instance to invoke a method on a remote
 * element. The [id] correlates this request with the eventual [InvocationResultEnvelope] or
 * [InvocationErrorEnvelope] response.
 *
 * @property id the unique identifier correlating this invocation with its response
 * @property payload the invocation request
 */
data class InvocationEnvelope(val id: String, override val payload: Invocation) : Envelope<Invocation> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION

}
