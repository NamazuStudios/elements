package com.namazustudios.socialengine.rt.mina;

import com.google.common.collect.Lists;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.Client;
import com.namazustudios.socialengine.rt.ClientContainer;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.DefaultClient;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.core.service.IoService;
import org.apache.mina.core.service.IoServiceListener;
import org.apache.mina.core.session.IdleStatus;
import org.apache.mina.core.session.IoSession;
import org.apache.mina.guice.MinaScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.net.SocketAddress;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Created by patricktwohig on 9/20/15.
 */
public class MinaClientContainer implements ClientContainer {

    private static final Logger LOG = LoggerFactory.getLogger(MinaClientContainer.class);

    @Inject
    @Named(Constants.TRANSPORT_RELIABLE)
    private Provider<Client> reliableClientProvider;

    @Inject
    @Named(Constants.TRANSPORT_BEST_EFFORT)
    private Provider<Client> bestEffortClientProvider;

    @Inject
    @Named(Constants.TRANSPORT_RELIABLE)
    private Provider<IoConnector> reliableIOConnectorProvider;

    @Inject
    @Named(Constants.TRANSPORT_BEST_EFFORT)
    private Provider<IoConnector> bestEffortIOConnectorProvider;

    @Override
    public ConnectedInstance connect(final SocketAddress socketAddress,
                                     final DisconnectHandler... disconnectHandlers) {

        final IoConnector reliableIoConnector = reliableIOConnectorProvider.get();
        final IoConnector bestEffortIoConnector = bestEffortIOConnectorProvider.get();

        final Client reliableClient = doConnect(reliableIoConnector, reliableClientProvider, socketAddress);;

        final Client bestEffortClient;

        try {
            bestEffortClient = doConnect(bestEffortIoConnector, bestEffortClientProvider, socketAddress);
        } catch (RuntimeException rex) {
            closeAndLog(reliableIoConnector);
            throw rex;
        }

        reliableIoConnector.addListener(new LoggingIoServiceListener());
        bestEffortIoConnector.addListener(new LoggingIoServiceListener());

        final List<DisconnectHandler> disconnectHandlerList = Lists.newArrayList(disconnectHandlers);

        for (final DisconnectHandler disconnectHandler : disconnectHandlerList) {

            reliableIoConnector.addListener(new IoServiceListenerAdaptor() {
                @Override
                public void sessionClosed(IoSession session) throws Exception {
                    disconnectHandler.didDisconnect(reliableClient, null);
                }
            });

            bestEffortIoConnector.addListener(new IoServiceListenerAdaptor() {
                @Override
                public void sessionClosed(IoSession session) throws Exception {
                    disconnectHandler.didDisconnect(bestEffortClient, null);
                }
            });

        }

        return new ConnectedInstance() {

            @Override
            public Client getRealiable() {
                return reliableClient;
            }

            @Override
            public Client getBestEffort() {
                return bestEffortClient;
            }

            @Override
            public void disconnect() {
                closeAndLog(reliableIoConnector);
                closeAndLog(bestEffortIoConnector);
            }

            @Override
            public void close() {
                disconnect();
            }

        };
    }

    private Client doConnect(final IoConnector ioConnector,
                             final Provider<Client> clientProvider,
                             final SocketAddress socketAddress) {

        final ConnectFuture connectFuture = ioConnector.connect(socketAddress);

        try {
            return MinaScopes.bootstrap(connectFuture, new Callable<Client>() {
                @Override
                public Client call() {
                    return clientProvider.get();
                }
            });
        } catch (Exception ex) {
            throw new InternalException(ex);
        }

    }

    private void closeAndLog(final IoConnector ioConnector) {
        try {
            ioConnector.dispose(true);
        } catch (RuntimeException rex) {
            LOG.error("Caught exception closing connector.", rex);
        }
    }


    private static class IoServiceListenerAdaptor implements IoServiceListener {

        @Override
        public void serviceActivated(IoService service) throws Exception {}

        @Override
        public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {}

        @Override
        public void serviceDeactivated(IoService service) throws Exception {}

        @Override
        public void sessionCreated(IoSession session) throws Exception {}

        @Override
        public void sessionClosed(IoSession session) throws Exception {}

        @Override
        public void sessionDestroyed(IoSession session) throws Exception {}

    }

    private static class LoggingIoServiceListener implements IoServiceListener {

        @Override
        public void serviceActivated(IoService service) throws Exception {
            LOG.info("Service activated {}", service);
        }

        @Override
        public void serviceIdle(IoService service, IdleStatus idleStatus) throws Exception {
            LOG.info("Service idle {}", service);
        }

        @Override
        public void serviceDeactivated(IoService service) throws Exception {
            LOG.info("Service deactivated {}", service);
        }

        @Override
        public void sessionCreated(IoSession session) throws Exception {
            LOG.info("Session created {}", session);
        }

        @Override
        public void sessionClosed(IoSession session) throws Exception {
            LOG.info("Session closed {}", session);
        }

        @Override
        public void sessionDestroyed(IoSession session) throws Exception {
            LOG.info("Session destroyed {}", session);
        }

    }
}
