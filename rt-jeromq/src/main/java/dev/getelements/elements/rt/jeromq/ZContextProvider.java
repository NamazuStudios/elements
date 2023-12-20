package dev.getelements.elements.rt.jeromq;

import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ZContextProvider implements Provider<ZContext> {

    public static final String IO_THREADS = "dev.getelements.elements.rt.jeromq.io.threads";

    public static final String MAX_SOCKETS = "dev.getelements.elements.rt.jeromq.max.sockets";

    private Provider<Integer> ioThreadsProvider;

    private Provider<Integer> maxSocketsProvider;

    @Override
    public ZContext get() {
        final ZContext zContext = new ZContext();
        zContext.getContext().setIOThreads(getIoThreadsProvider().get());
        zContext.getContext().setMaxSockets(getMaxSocketsProvider().get());
        return zContext;
    }

    public Provider<Integer> getIoThreadsProvider() {
        return ioThreadsProvider;
    }

    @Inject
    public void setIoThreadsProvider(@Named(IO_THREADS) Provider<Integer> ioThreadsProvider) {
        this.ioThreadsProvider = ioThreadsProvider;
    }

    public Provider<Integer> getMaxSocketsProvider() {
        return maxSocketsProvider;
    }

    @Inject
    public void setMaxSocketsProvider(@Named(MAX_SOCKETS) Provider<Integer> maxSocketsProvider) {
        this.maxSocketsProvider = maxSocketsProvider;
    }

}
