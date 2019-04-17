package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.jeromq.*;
import com.namazustudios.socialengine.rt.util.SyncWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.*;

import com.namazustudios.socialengine.rt.jeromq.MessageManager.MessageManagerConfiguration;

import static org.zeromq.ZContext.shadow;
import static org.zeromq.ZMQ.*;

/**
 * Threaded manager for a multiplexed ZMQ connection.
 */
public class JeroMQMultiplexedConnectionRunnable implements Runnable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQMultiplexedConnectionRunnable.class);

    private final String controlAddress;

    private final ZContext zContext;

    private final SyncWait<Void> threadBlocker = new SyncWait<>(logger);

    public JeroMQMultiplexedConnectionRunnable(final String controlAddress,
                                               final ZContext zContext) {
        this.controlAddress = controlAddress;
        this.zContext = zContext;
    }

    public void blockCurrentThreadUntilControlChannelIsConnected() {
        threadBlocker.get();
    }

    @Override
    public void run() {
        final MessageManagerConfiguration messageManagerConfiguration =
                new MessageManagerConfiguration(false);

        try (final ZContext context = shadow(zContext);
             final MessageManager messageManager = new MessageManager(messageManagerConfiguration);
             final ConnectionsManager connectionsManager = new ConnectionsManager()
        ) {

            connectionsManager.registerSetupHandler(cm -> {
                logger.info("Binding control socket....");
                final int controlSocketHandle = cm.bindToAddressAndBeginPolling(
                        controlAddress,
                        PULL,
                        messageManager::handleControlMessage,
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