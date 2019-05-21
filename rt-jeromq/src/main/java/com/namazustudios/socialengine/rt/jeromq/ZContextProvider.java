package com.namazustudios.socialengine.rt.jeromq;

import org.zeromq.ZContext;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ZContextProvider implements Provider<ZContext> {

    public static final String IO_THREADS = "com.namazustudios.socialengine.rt.jeromq.io.threads";

    public static final String MAX_SOCKETS = "com.namazustudios.socialengine.rt.jeromq.max.sockets";

    private Provider<Integer> ioThreadsProvier;

    private Provider<Integer> maxSocketsProvider;

    @Override
    public ZContext get() {
        final ZContext zContext = new ZContext();
        zContext.getContext().setIOThreads(getIoThreadsProvier().get());
        zContext.getContext().setMaxSockets(getMaxSocketsProvider().get());
        return zContext;
    }

    public Provider<Integer> getIoThreadsProvier() {
        return ioThreadsProvier;
    }

    @Inject
    public void setIoThreadsProvier(@Named(IO_THREADS) Provider<Integer> ioThreadsProvier) {
        this.ioThreadsProvier = ioThreadsProvier;
    }

    public Provider<Integer> getMaxSocketsProvider() {
        return maxSocketsProvider;
    }

    @Inject
    public void setMaxSocketsProvider(@Named(MAX_SOCKETS) Provider<Integer> maxSocketsProvider) {
        this.maxSocketsProvider = maxSocketsProvider;
    }

}
