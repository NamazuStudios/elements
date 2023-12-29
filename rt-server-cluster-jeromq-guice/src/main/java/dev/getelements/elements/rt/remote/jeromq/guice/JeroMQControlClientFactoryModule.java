package dev.getelements.elements.rt.remote.jeromq.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.AsyncConnectionService;
import dev.getelements.elements.rt.remote.AsyncControlClient;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.jeromq.JeroMQAsyncControlClient;
import dev.getelements.elements.rt.remote.jeromq.JeroMQControlClient;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

public class JeroMQControlClientFactoryModule extends PrivateModule {
    @Override
    protected void configure() {

        final var zContextProvider = getProvider(ZContext.class);
        final var jeroMQSecurityChainProvider = getProvider(JeroMQSecurity.class);

        bind(ControlClient.Factory.class).toInstance(ca -> new JeroMQControlClient(
                zContextProvider.get(),
                ca,
                jeroMQSecurityChainProvider.get())
        );

        final var key = Key.get(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){});
        final var asp = getProvider(key);
        bind(AsyncControlClient.Factory.class).toInstance(ca -> new JeroMQAsyncControlClient(
                asp.get(),
                ca,
                jeroMQSecurityChainProvider.get())
        );

        expose(ControlClient.Factory.class);
        expose(AsyncControlClient.Factory.class);

    }
}
