package dev.getelements.elements.cluster.client

import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope.Mode
import dev.getelements.elements.sdk.cluster.remote.InvocationErrorConsumer
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult
import dev.getelements.elements.sdk.model.exception.InternalException
import jakarta.websocket.ClientEndpoint
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
import java.util.function.Consumer

@ClientEndpoint
class JakartaWebsocketRemoteInvocation(
    val id : String,
    private val session : Session,
    private val invocation: Invocation,
    private val asyncInvocationResultConsumerList: List<Consumer<InvocationResult>>,
    private val asyncInvocationErrorConsumer: InvocationErrorConsumer
) : RemoteInvocationState {

    companion object {
        val logger: Logger = LoggerFactory.getLogger(JakartaWebsocketRemoteInvocation::class.java)
    }

    private val mutex: Mutex = Mutex()

    private val scope = CoroutineScope(Dispatchers.Default + SupervisorJob())

    private var error: Throwable? = null

    private var syncError: InvocationErrorEnvelope? = null

    private var syncResult: InvocationResultEnvelope? = null

    private var asyncError : InvocationErrorEnvelope? = null

    private var asyncCompleted: BitSet = BitSet(asyncInvocationResultConsumerList.size)

    fun send() : CompletableFuture<Any> = scope.future {

        logger.trace("Sending invocation.")
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

    override suspend fun onError(throwable: Throwable) {
        mutex.withLock {
            this@JakartaWebsocketRemoteInvocation.error = throwable
        }
    }

    override suspend fun onInvocationError(error: InvocationErrorEnvelope) {
        asyncInvocationErrorConsumer.accept(error.payload)
    }

    override suspend fun onInvocationResult(result: InvocationResultEnvelope) {

        mutex.withLock {
            when(result.mode) {
                Mode.SYNC -> syncResult = result
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
            }

        }

        asyncInvocationResultConsumerList.get(result.param).accept(result.payload)

    }

}
