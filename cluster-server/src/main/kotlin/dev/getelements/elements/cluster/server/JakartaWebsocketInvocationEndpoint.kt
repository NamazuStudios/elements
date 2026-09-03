package dev.getelements.elements.cluster.server

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.common.dto.Envelope.Type.INVOCATION
import dev.getelements.elements.sdk.cluster.remote.RemoteInvocationDispatcher
import dev.getelements.elements.sdk.cluster.remote.dto.Invocation
import jakarta.websocket.*
import jakarta.websocket.server.ServerEndpoint

@ServerEndpoint("/remote")
class JakartaWebsocketInvocationEndpoint() {

    lateinit var dispatcher: RemoteInvocationDispatcher

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {

    }

    @OnMessage
    fun onMessage(session: Session, envelope: Envelope<Any>) {
        when (envelope.type) {
            INVOCATION -> dispatcher.dispatch(envelope.payload as Invocation, session.asyncRemote::sendObject)
            else -> throw IllegalArgumentException("Envelope type ${envelope.type} is not supported")
        }
    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {

    }

}
