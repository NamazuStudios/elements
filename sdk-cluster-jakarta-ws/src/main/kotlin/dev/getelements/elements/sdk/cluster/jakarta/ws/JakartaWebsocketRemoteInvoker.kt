package dev.getelements.elements.sdk.cluster.jakarta.ws

import dev.getelements.elements.sdk.cluster.remote.AsyncOperation
import dev.getelements.elements.sdk.cluster.remote.Invocation
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer
import dev.getelements.elements.sdk.cluster.remote.InvocationResult
import dev.getelements.elements.sdk.cluster.remote.RemoteInvoker
import dev.getelements.elements.sdk.util.io.PayloadReader
import dev.getelements.elements.sdk.util.io.PayloadWriter
import java.util.concurrent.CompletionStage
import java.util.function.Consumer

class JakartaWebsocketRemoteInvoker(
    private val connectAddress: String,
    private val payloadReader: PayloadReader,
    private val payloadWriter: PayloadWriter
) : RemoteInvoker {

    override fun invokeAsync(
        invocation: Invocation,
        asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
        asyncInvocationErrorConsumer: InvocationErrorConsumer
    ): AsyncOperation? = null

    override fun invokeCompletionStage(
        invocation: Invocation,
        asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
        asyncInvocationErrorConsumer: InvocationErrorConsumer
    ): CompletionStage<Any> = JakartaWebsocketRemoteInvocation(
        invocation,
        asyncInvocationResultConsumerList,
        asyncInvocationErrorConsumer).send();

}
