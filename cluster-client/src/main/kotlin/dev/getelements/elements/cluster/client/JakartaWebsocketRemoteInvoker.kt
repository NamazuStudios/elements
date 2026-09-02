package dev.getelements.elements.cluster.client

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.common.dto.InstanceMetadataEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.sdk.Subscription
import dev.getelements.elements.sdk.cluster.remote.AsyncOperation
import dev.getelements.elements.sdk.cluster.remote.InstanceRemoteInvoker
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer
import dev.getelements.elements.sdk.cluster.remote.dto.InstanceMetadata
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult
import dev.getelements.elements.sdk.util.ConcurrentDequePublisher
import jakarta.websocket.*
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletionStage
import java.util.concurrent.atomic.AtomicLong
import java.util.concurrent.atomic.AtomicReference
import java.util.function.Consumer

@ClientEndpoint
class JakartaWebsocketRemoteInvoker : InstanceRemoteInvoker {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JakartaWebsocketRemoteInvoker::class.java)
    }

    private val sequence = AtomicLong(0)

    private val session: AtomicReference<Session> = AtomicReference()

    private val operations : SortedMap<String, RemoteInvocationState> = TreeMap();

    private val instanceMetadata = AtomicReference(InstanceMetadata.DEFAULT)

    private val instanceMetadataPublisher = ConcurrentDequePublisher<InstanceMetadata>(JakartaWebsocketRemoteInvoker::class.java);

    override fun invokeAsync(
        invocation: Invocation,
        asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
        asyncInvocationErrorConsumer: InvocationErrorConsumer
    ): AsyncOperation {

        val id = sequence.getAndIncrement().toString()
        val session = this.session.get() ?: throw IllegalStateException("Session is null.")

        val operation = JakartaWebsocketAsyncOperation(
            id,
            session,
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer
        )

        operations[operation.id] = operation
        operation.send()

        return operation

    }

    override fun invokeCompletionStage(
        invocation: Invocation,
        asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
        asyncInvocationErrorConsumer: InvocationErrorConsumer
    ): CompletionStage<Any> {

        val id = sequence.getAndIncrement().toString()
        val session = this.session.get() ?: throw IllegalStateException("Session is null.")

        val invocation = JakartaWebsocketRemoteInvocation(
            id,
            session,
            invocation,
            asyncInvocationResultConsumerList,
            asyncInvocationErrorConsumer
        )

        operations[invocation.id] = invocation
        return invocation.send()

    }

    @OnOpen
    suspend fun onOpen(session: Session) {
        this.session.set(session)
    }

    @OnError
    suspend fun onError(session: Session, throwable: Throwable) {
        operations.values.forEach { it.onError(throwable) }
    }

    @OnMessage
    suspend fun onMessage(session: Session, envelope: Envelope<Any>) {
        when (envelope) {
            is InvocationErrorEnvelope -> onInvocationError(envelope)
            is InvocationResultEnvelope -> onInvocationResult(envelope)
            is InstanceMetadataEnvelope -> onInstanceMetadata(envelope)
            else -> throw IllegalArgumentException("Envelope type ${envelope.type} is not supported")
        }
    }

    private suspend fun onInvocationError(envelope: InvocationErrorEnvelope) {
        val operation = operations[envelope.id]
        if (operation == null) logger.error("Unknown result invocation {}", envelope.id)
        operation?.onInvocationError(envelope)
    }

    private suspend fun onInvocationResult(envelope: InvocationResultEnvelope) {
        val operation = operations[envelope.id]
        if (operation == null) logger.error("Unknown result invocation {}", envelope.id)
        operation?.onInvocationResult(envelope)
    }

    private fun onInstanceMetadata(envelope: InstanceMetadataEnvelope) {
        instanceMetadata.set(envelope.payload)
        instanceMetadataPublisher.publish(envelope.payload)
    }

    private fun getInvocationState(id:String) : RemoteInvocationState {
        return operations[id] ?: throw IllegalStateException("Session is null.")
    }

    override fun getInstanceMetadata(): InstanceMetadata? = instanceMetadata.get()

    override fun onMetadataUpdate(onMetadata: Consumer<InstanceMetadata?>?): Subscription? = instanceMetadataPublisher.subscribe(onMetadata)

}
