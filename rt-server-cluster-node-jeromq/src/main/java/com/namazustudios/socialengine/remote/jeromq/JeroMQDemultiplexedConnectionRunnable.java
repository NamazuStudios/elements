package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import com.namazustudios.socialengine.rt.jeromq.MessageManager.MessageManagerConfiguration;

import java.util.Set;
import java.util.UUID;

import static com.namazustudios.socialengine.rt.jeromq.MessageManager.MessageManagerConfiguration.Strategy.DEMULTIPLEX;

import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

/**
 * Threaded manager for a demultiplexed ZMQ connection.
 */
public class JeroMQDemultiplexedConnectionRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQDemultiplexedConnectionRunnable.class);

    private final Set<String> controlAddresses;

    private final ZContext zContext;

    private final SyncWait<Void> threadBlocker = new SyncWait<>(logger);

    private final UUID instanceUuid;

    public JeroMQDemultiplexedConnectionRunnable(final Set<String> controlAddresses,
                                                 final UUID instanceUuid,
                                                 final ZContext zContext) {
        this.controlAddresses = controlAddresses;
        this.instanceUuid = instanceUuid;
        this.zContext = zContext;
    }

    public void blockCurrentThreadUntilControlChannelIsConnected() {
        threadBlocker.get();
    }

    @Override
    public void run() {
        final MessageManagerConfiguration messageManagerConfiguration =
                new MessageManagerConfiguration(DEMULTIPLEX, instanceUuid, true);

        try (final ZContext context = shadow(zContext);
             final MessageManager messageManager = new MessageManager(messageManagerConfiguration);
             final ConnectionsManager connectionsManager = new ConnectionsManager()
        ) {

            connectionsManager.registerSetupHandler(cm -> {
                logger.info("Binding control socket....");
                final int controlSocketHandle = cm.bindToAddressesAndBeginPolling(
                        controlAddresses,
                        REP,
                        messageManager::handleBoundControlMessage,
                        false
                );
                logger.info("Successfully bound control socket to handle: {}.", controlSocketHandle);

                // unblock the thread
                threadBlocker.getResultConsumer().accept(null);
            });

            connectionsManager.start(context);
        }
    }
}