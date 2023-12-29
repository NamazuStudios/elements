package dev.getelements.elements.rt.remote.jeromq.guice;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity;
import dev.getelements.elements.rt.remote.jeromq.JeroMQSecurityProvider;

public class JeroMQSecurityModule extends PrivateModule {

    @Override
    protected void configure() {

        bind(JeroMQSecurity.class)
                .toProvider(JeroMQSecurityProvider.class)
                .asEagerSingleton();

        expose(JeroMQSecurity.class);

    }

}
