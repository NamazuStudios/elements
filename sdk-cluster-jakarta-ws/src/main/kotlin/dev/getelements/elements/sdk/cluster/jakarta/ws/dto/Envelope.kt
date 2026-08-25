package dev.getelements.elements.sdk.cluster.jakarta.ws.dto

sealed interface Envelope<PayloadT> {

    val type: Type

    val payload: PayloadT

    enum class Type {
        INVOCATION,
        INVOCATION_RESULT,
        INVOCATION_ERROR
    }

}
