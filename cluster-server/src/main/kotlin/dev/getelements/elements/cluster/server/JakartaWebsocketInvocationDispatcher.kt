package dev.getelements.elements.cluster.server

import dev.getelements.elements.cluster.common.dto.Envelope
import dev.getelements.elements.cluster.server.Constants.ELEMENT_REGISTRY
import dev.getelements.elements.sdk.ElementRegistry
import jakarta.websocket.EndpointConfig
import jakarta.websocket.OnError
import jakarta.websocket.OnMessage
import jakarta.websocket.OnOpen
import jakarta.websocket.Session
import jakarta.websocket.server.ServerEndpoint

@ServerEndpoint("/remote")
class JakartaWebsocketInvocationDispatcher() {

    lateinit var registry: ElementRegistry

    @OnOpen
    fun onOpen(session: Session, config: EndpointConfig) {
        registry = config.userProperties[ELEMENT_REGISTRY] as ElementRegistry
    }

    @OnMessage
    fun onMessage(session: Session, envelope: Envelope<Any>) {

    }

    @OnError
    fun onError(session: Session, throwable: Throwable) {

    }

}
