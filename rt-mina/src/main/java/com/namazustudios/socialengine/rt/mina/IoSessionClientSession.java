package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.Event;
import com.namazustudios.socialengine.rt.EventReceiver;
import com.namazustudios.socialengine.rt.SimpleEvent;
import com.namazustudios.socialengine.rt.edge.AbstractEdgeClientSession;
import com.namazustudios.socialengine.rt.edge.EdgeClientSession;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * Created by patricktwohig on 8/1/15.
 */
public class IoSessionClientSession extends AbstractEdgeClientSession implements EdgeClientSession {

    @Inject
    private IoSession ioSession;

    @Inject
    private EdgeServer edgeServer;

    @Override
    public String getId() {
        return Long.toString(ioSession.getId());
    }

    @Override
    public void setSessionVariable(Object key, Object value) {
        ioSession.setAttribute(new SessionKey(key), value);
    }

    @Override
    public Object setSessionVariableIfAbsent(Object key, Object value) {
        return ioSession.setAttributeIfAbsent(new SessionKey(key), value);
    }

    @Override
    public Object getSessionVariable(Object key) {
        return getSessionVariable(key, null);
    }

    @Override
    public Object getSessionVariable(Object key, Object defaultValue) {
        return ioSession.getAttribute(new SessionKey(key), defaultValue);
    }

    @Override
    public <T> T getSessionVariableTyped(Object key, Class<T> type) {
        return getSessionVariableTyped(key, type, null);
    }

    @Override
    public <T> T getSessionVariableTyped(Object key, Class<T> type, T defaultValue) {
        final Object object = ioSession.getAttribute(new SessionKey(key), defaultValue);
        return type.cast(object);
    }

    @Override
    public void removeSessionVariable(Object key) {
        ioSession.removeAttribute(new SessionKey(key));
    }

    @Override
    public <T> EventReceiver<T> getEventReceiver(final Class<T> type) {
        return new EventReceiver<T>() {
            @Override
            public Class<T> getEventType() {
                return type;
            }

            @Override
            public void receive(Event event) {

                final SimpleEvent simpleEvent = SimpleEvent.builder()
                        .event(event)
                    .build();

                ioSession.write(simpleEvent);

            }
        };
    }

    /**
     * Gets the underlying {@link IoSession} instance.
     *
     * @return the underlying {@link IoSession} instance.
     */
    public IoSession getIoSession() {
        return ioSession;
    }

    public class SessionKey {

        final Object key;

        public SessionKey(Object key) {
            this.key = key;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof SessionKey)) return false;
            final SessionKey that = (SessionKey) o;
            return !(key != null ? !key.equals(that.key) : that.key != null);
        }

        @Override
        public int hashCode() {
            return key != null ? key.hashCode() : 0;
        }

    }

    @Override
    public void disconnect() {
        edgeServer.post(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                ioSession.close(false);
                return null;
            }
        });
    }

}
