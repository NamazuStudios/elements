package dev.getelements.elements.appnode;

import com.google.inject.Guice;
import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.guice.ConfigurationModule;
import dev.getelements.elements.rt.remote.ControlClient;
import dev.getelements.elements.rt.remote.InstanceStatus;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQAsyncConnectionServiceModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQControlClientFactoryModule;
import dev.getelements.elements.rt.remote.jeromq.guice.JeroMQControlClientModule;
import dev.getelements.elements.rt.remote.jeromq.guice.ZContextModule;
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
