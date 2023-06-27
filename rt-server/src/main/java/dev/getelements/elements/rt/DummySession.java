package dev.getelements.elements.rt;

import dev.getelements.elements.rt.handler.HandlerClientSessionObserver;
import dev.getelements.elements.rt.handler.Session;

import java.io.Serializable;

public class DummySession implements Session, Serializable {

    private static DummySession INSTANCE = new DummySession();

    /**
     * Returns a singleton {@link DummySession} instance.
     *
     * @return the {@link DummySession} instane
     */
    public static DummySession getDummySession() {
        return INSTANCE;
    }

    @Override
    public String getId() {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public void setSessionVariable(Object key, Object value) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public Object setSessionVariableIfAbsent(Object key, Object value) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public Object getSessionVariable(Object key, Object defaultValue) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public void removeSessionVariable(Object key) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public Observation observeIdle(HandlerClientSessionObserver handlerClientSessionObserver) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public Observation observeDisconnect(HandlerClientSessionObserver handlerClientSessionObserver) {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

    @Override
    public void disconnect() {
        throw new UnsupportedOperationException("Session Not Supported.");
    }

}
