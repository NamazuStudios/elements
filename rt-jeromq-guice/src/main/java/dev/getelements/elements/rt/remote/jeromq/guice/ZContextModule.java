package dev.getelements.elements.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.jeromq.ZContextProvider;
import org.zeromq.ZContext;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.jeromq.ZContextProvider.*;
import static java.lang.Runtime.getRuntime;

public class ZContextModule extends PrivateModule {

    private Runnable ipv6Action = () -> {};

    private Runnable ioThreadsAction = () -> {};

    private Runnable maxSocketsAction = () -> {};

    @Override
    protected void configure() {
        ipv6Action.run();
        ioThreadsAction.run();
        maxSocketsAction.run();
        bind(ZContext.class).toProvider(ZContextProvider.class).asEagerSingleton();
        expose(ZContext.class);
    }

    public ZContextModule withDefaultIpv6() {
        return withIpv6(true);
    }

    public ZContextModule withIpv6(final boolean ipv6) {
        bind(Boolean.class).annotatedWith(named(IPV6)).toInstance(ipv6);
        return this;
    }

    public ZContextModule withDefaultIoThreads() {
        final int ioThreads = getRuntime().availableProcessors() + 1;
        return withIoThreads(ioThreads);
    }

    public ZContextModule withIoThreads(final int ioThreads) {
        ioThreadsAction = () -> bind(Integer.class).annotatedWith(named(IO_THREADS)).toInstance(ioThreads);
        return this;
    }

    public ZContextModule withDefaultMaxSockets() {
        return withMaxSockets(zmq.ZMQ.ZMQ_MAX_SOCKETS_DFLT);
    }

    public ZContextModule withMaxSockets(int maxSockets) {
        maxSocketsAction = () -> bind(Integer.class).annotatedWith(named(MAX_SOCKETS)).toInstance(maxSockets);
        return this;
    }

}
