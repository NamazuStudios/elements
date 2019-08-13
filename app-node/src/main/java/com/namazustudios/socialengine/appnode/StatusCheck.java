package com.namazustudios.socialengine.appnode;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.namazustudios.socialengine.guice.ZContextModule;
import com.namazustudios.socialengine.rt.remote.ControlClient;
import com.namazustudios.socialengine.rt.remote.InstanceStatus;
import com.namazustudios.socialengine.rt.remote.jeromq.guice.JeroMQControlClientModule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StatusCheck {

    private static final Logger logger = LoggerFactory.getLogger(StatusCheck.class);

    private final String connectAddress;

    public StatusCheck(final String connectAddress) {
        this.connectAddress = connectAddress;
    }

    public void run() {

        final Injector injector = Guice.createInjector(new ZContextModule(), new JeroMQControlClientModule());
        final ControlClient.Factory controlClientFactory = injector.getInstance(ControlClient.Factory.class);

        try (final ControlClient client = controlClientFactory.open(connectAddress)) {
            final InstanceStatus instanceStatus = client.getInstanceStatus();
            final StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Instance status: ").append(instanceStatus.getInstanceId().asString()).append('\n');
            instanceStatus.getNodeIds().forEach(nid -> stringBuilder.append("Node: " ).append(nid.asString()));
            logger.info("{}", stringBuilder);
        }

    }

}
