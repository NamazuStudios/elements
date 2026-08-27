package dev.getelements.elements.sdk.cluster.jakarta.ws

import dev.getelements.elements.sdk.cluster.jakarta.ws.dto.Envelope
import dev.getelements.elements.sdk.cluster.jakarta.ws.dto.Envelope.Type
import dev.getelements.elements.sdk.cluster.jakarta.ws.dto.InvocationErrorEnvelope
import dev.getelements.elements.sdk.cluster.jakarta.ws.dto.InvocationResultEnvelope
import dev.getelements.elements.sdk.cluster.jakarta.ws.dto.InvocationResultEnvelope.Mode
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult
import dev.getelements.elements.sdk.model.exception.InternalException
import jakarta.websocket.ClientEndpoint
import jakarta.websocket.OnMessage
import jakarta.websocket.Session
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.future.future
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.yield
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.concurrent.CompletableFuture

@ClientEndpoint
class JakartaWebsocketRemoteInvocation(
    private val invocation: Invocation,
    private val asyncInvocationResultConsumerList: List<java.util.function.Consumer<InvocationResult>>,
    private val asyncInvocationErrorConsumer: InvocationErrorConsumer
) {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JakartaWebsocketRemoteInvocation::class.java)
    }

    private val mutex: Mutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var session: Session? = null

    private var error: Throwable? = null

    private var syncError: InvocationErrorEnvelope? = null

    private var syncResult: InvocationResultEnvelope? = null

    private var asyncError : InvocationErrorEnvelope? = null

    private var asyncCompleted: BitSet = BitSet(asyncInvocationResultConsumerList.size)

    fun send() : CompletableFuture<Any> = scope.future {

        logger.trace("Waiting for connection to send invocation.")

        mutex.withLock {
            while (session == null && error == null) {
                yield()
            }
        }

        if (error != null) {
            throw error!!
        } else if (session != null) {
            throw IllegalStateException("Failed to get Session and no error was raised.")
        }

        logger.trace("Connected. Sending invocation.")
        session?.asyncRemote?.sendObject(invocation)

        logger.trace("Waiting for all asynchronous responses to return.")
        asyncCompleted.set(0, asyncInvocationResultConsumerList.size)

        val params = asyncInvocationResultConsumerList.size

        mutex.withLock {
            while (error == null && asyncError == null && asyncCompleted.size() < params)
                yield()
        }

        if (error != null) {
            throw error!!
        }

        logger.trace("Waiting for sync responses.")

        mutex.withLock {
            while (error == null && syncError == null && syncResult == null)
                yield()
        }

        if (error != null) {
            throw error!!
        } else if (syncError != null) {
            throw syncError?.payload?.throwable as Throwable
        }

        logger.trace("Response sent. Returning sync result.")
        syncResult!!.payload

    }

    @OnMessage
    suspend fun onConnection(session: Session) {
        mutex.withLock {
            this@JakartaWebsocketRemoteInvocation.session = session
        }
    }

    @OnMessage
    suspend fun onError(session: Session, throwable: Throwable) {
        mutex.withLock {
            this@JakartaWebsocketRemoteInvocation.error = throwable
        }
    }

    @OnMessage
    suspend fun onMessage(session: Session, envelope: Envelope<Any>) {
        when(envelope.type) {
            Type.INVOCATION_ERROR -> onInvocationError(envelope as InvocationErrorEnvelope)
            Type.INVOCATION_RESULT -> onInvocationResult(envelope as InvocationResultEnvelope)
            else -> fail(envelope)
        }
    }

    fun onInvocationError(envelope: InvocationErrorEnvelope) {
        asyncInvocationErrorConsumer.accept(envelope.payload)
    }

    suspend fun onInvocationResult(envelope: InvocationResultEnvelope) {

        mutex.withLock {
            when(envelope.mode) {
                Mode.SYNC -> syncResult = envelope
                Mode.ASYNC -> {

                    if (asyncCompleted.get(envelope.param)) {
                        error = InternalException("Duplicate result in async response param:${envelope.param}")
                        throw error as Throwable
                    }

                    val params = asyncInvocationResultConsumerList.size

                    if (envelope.param >= params) {
                        error = InternalException("Parameter out of bounds: ${envelope.param} >= $params")
                        throw error as Throwable
                    }

                    asyncCompleted.set(envelope.param)

                }
            }

        }

        asyncInvocationResultConsumerList.get(envelope.param).accept(envelope.payload)

    }

    private fun fail(message: Envelope<Any>) {
        error = InternalException("Received unexpected message type:${message.type}}")
    }

}
