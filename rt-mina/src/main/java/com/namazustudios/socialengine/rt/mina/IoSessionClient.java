package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.rt.edge.EdgeClient;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import org.apache.mina.core.session.IoSession;

import javax.inject.Inject;
import java.util.concurrent.Callable;

/**
 * Created by patricktwohig on 8/1/15.
 */
public class IoSessionClient implements EdgeClient {

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
    public <T> T getSessionVariable(Object key, Class<T> type) {
        return getSessionVariable(key, type, null);
    }

    @Override
    public <T> T getSessionVariable(Object key, Class<T> type, T defaultValue) {
        final Object object = ioSession.getAttribute(new SessionKey(key), defaultValue);
        return type.cast(object);
    }

    @Override
    public void removeSessionVariable(Object key) {
        ioSession.removeAttribute(new SessionKey(key));
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
