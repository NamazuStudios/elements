package dev.getelements.elements.cluster.server

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INVOCATION
import dev.getelements.elements.cluster.common.dto.InvocationEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationErrorEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope
import dev.getelements.elements.cluster.common.dto.InvocationResultEnvelope.Mode
import dev.getelements.elements.cluster.server.Constants.INVOCATION_DISPATCHER
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher.ResultAsyncHandler
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationError
import dev.getelements.elements.sdk.cluster.remote.dto.InvocationResult

@ServerEndpoint("/remote")
class JakartaWebsocketInvocationEndpoint() {

    lateinit var dispatcher: RemoteInvocationDispatcher

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        dispatcher = config.userProperties[INVOCATION_DISPATCHER] as RemoteInvocationDispatcher
    }

    @OnMessage
    fun onMessage(session: Session, envelope: Envelope<Any>) {
        when (envelope.type) {
            INVOCATION -> doDispatch(session, envelope as InvocationEnvelope)
            else -> throw IllegalArgumentException("Envelope type ${envelope.type} is not supported")
        }
    }

    fun doDispatch(session: Session, envelope: InvocationEnvelope) {

        val handler = object : ResultAsyncHandler {

            override fun onInvocationResult(result: InvocationResult) {
                val mode = if (result.param() == InvocationResult.RETURN_VALUE) Mode.SYNC else Mode.ASYNC
                session.asyncRemote.sendObject(InvocationResultEnvelope(envelope.id, mode, result.param(), result))
            }

            override fun onInvocationError(error: InvocationError) {
                session.asyncRemote.sendObject(InvocationErrorEnvelope(envelope.id, error))
            }

        }

        dispatcher.dispatch(envelope.payload, handler)

    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {

    }

}
