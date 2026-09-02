package dev.getelements.elements.cluster.client

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope.Mode
import dev.getelements.elements.sdk.cluster.remote.AsyncOperation
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult
import dev.getelements.elements.sdk.model.exception.InternalException
import jakarta.websocket.Session
import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.util.*
import java.util.function.Consumer

class JakartaWebsocketAsyncOperation(
    val id : String,
    private val session : Session,
    private val invocation: Invocation,
    private val asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
    private val asyncInvocationErrorConsumer: InvocationErrorConsumer
) : AsyncOperation, RemoteInvocationState {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JakartaWebsocketAsyncOperation::class.java)
    }

    private val mutex: Mutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var error: Throwable? = null

    private var asyncError : InvocationErrorEnvelope? = null

    private var asyncCompleted: BitSet = BitSet(asyncInvocationResultConsumerList.size)

    @Volatile
    private var canceled: Boolean = false

    fun send() = scope.launch {

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

    }

    override suspend fun onError(throwable: Throwable) {
        mutex.withLock {
            this@JakartaWebsocketAsyncOperation.error = throwable
        }
    }

    override suspend fun onInvocationError(error: InvocationErrorEnvelope) {
        asyncInvocationErrorConsumer.accept(error.payload)
    }

    override suspend fun onInvocationResult(result: InvocationResultEnvelope) {

        mutex.withLock {
            when(result.mode) {
                Mode.ASYNC -> {

                    if (asyncCompleted.get(result.param)) {
                        error = InternalException("Duplicate result in async response param:${result.param}")
                        throw error as Throwable
                    }

                    val params = asyncInvocationResultConsumerList.size

                    if (result.param >= params) {
                        error = InternalException("Parameter out of bounds: ${result.param} >= $params")
                        throw error as Throwable
                    }

                    asyncCompleted.set(result.param)

                }
                else -> fail(result)
            }

        }

        asyncInvocationResultConsumerList[result.param].accept(result.payload)

    }

    private fun fail(envelope: Envelope<*>) {
        error = InternalException("Received unexpected message type:${envelope.type}}")
    }

    override fun cancel() {
        canceled = true
    }

}
