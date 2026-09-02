package dev.getelements.elements.cluster.common.dto

sealed interface Envelope<PayloadT> {

    val type: Type

    val payload: PayloadT

    enum class Type {
        INVOCATION,
        INVOCATION_RESULT,
        INVOCATION_ERROR,
        INSTANCE_METADATA
    }

}
