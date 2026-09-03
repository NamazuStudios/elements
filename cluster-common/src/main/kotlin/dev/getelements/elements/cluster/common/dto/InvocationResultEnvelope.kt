package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult

/**
 * Wraps the successful result of a remote [dev.getelements.elements.sdk.cluster.remote.dto.Invocation],
 * returned to the caller identified by [id]. Depending on [mode], this either carries the invocation's
 * synchronous return value, or one of its asynchronous callback parameters (identified by [param]).
 *
 * @property id the unique identifier correlating this result with its originating [InvocationEnvelope]
 * @property mode the mode of the response to indicate how it should be handled
 * @property param the param the parameter number, used only for [Mode.ASYNC]
 * @property payload the payload object
 */
data class InvocationResultEnvelope(
    val id: String,
    val mode: Mode,
    val param: Int,
    override val payload: InvocationResult
) : Envelope<InvocationResult> {

    override val type: Envelope.Type = Envelope.Type.INVOCATION_RESULT

    /**
     * Indicates how the consumer of an [InvocationResultEnvelope] should interpret [payload].
     */
    enum class Mode {

        /**
         * Synchronous return value. Maps to the value of the function's return type.
         */
        SYNC,

        /**
         * Asynchronous return value. Maps to the asynchronous type, which is one of the positional arguments.
         */
        ASYNC

    }

}
