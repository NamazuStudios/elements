package com.namazustudios.socialengine.appnode;

import com.google.inject.Guice;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import com.namazustudios.socialengine.guice.ConfigurationModule;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQAsyncConnectionServiceModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQControlClientFactoryModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQControlClientModule;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.ZContextModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCheck {

    private static final Logger logger = LoggerFactory.getLogger(StatusCheck.class);

    private final String connectAddress;

    public StatusCheck(final String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public void run() {

        final var configurationSupplier = new DefaultConfigurationSupplier();

        final var injector = Guice.createInjector(
            new ZContextModule(),
            new ConfigurationModule(configurationSupplier),
            new JeroMQAsyncConnectionServiceModule(),
            new JeroMQControlClientFactoryModule()
        );

        final var controlClientFactory = injector.getInstance(ControlClient.Factory.class);

        try (final var client = controlClientFactory.open(connectAddress)) {
            final InstanceStatus instanceStatus = client.getInstanceStatus();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Instance status: ").append(instanceStatus.getInstanceId().asString()).append('\n');
            instanceStatus.getNodeIds().forEach(nid -> stringBuilder.append("  Node: " ).append(nid.asString()));
            logger.info("{}", stringBuilder);
        }

    }

}
