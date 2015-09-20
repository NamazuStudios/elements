package com.namazustudios.socialengine.rt.mina;

import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.rt.Client;
import com.namazustudios.socialengine.rt.ClientContainer;
import com.namazustudios.socialengine.rt.Constants;
import com.namazustudios.socialengine.rt.DefaultClient;
import org.apache.mina.core.future.ConnectFuture;
import org.apache.mina.core.service.IoConnector;
import org.apache.mina.guice.MinaScopes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import java.net.SocketAddress;
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

}
