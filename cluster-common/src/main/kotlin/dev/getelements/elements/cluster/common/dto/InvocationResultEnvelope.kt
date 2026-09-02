package dev.getelements.elements.cluster.common.dto

import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult

/**
 * The invocation result envelope. Stores the response payload as well as wire info
 *
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
     * The mode of the response.
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
