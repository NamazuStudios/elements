package com.namazustudios.socialengine.remote.jeromq;

import com.namazustudios.socialengine.rt.PayloadReader;
import com.namazustudios.socialengine.rt.PayloadWriter;
import com.namazustudios.socialengine.rt.remote.Invocation;
import com.namazustudios.socialengine.rt.remote.InvocationError;
import com.namazustudios.socialengine.rt.remote.InvocationResult;
import com.namazustudios.socialengine.rt.remote.RemoteInvoker;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.util.List;
import java.util.concurrent.Future;
import java.util.function.BiFunction;
import java.util.function.Consumer;
import java.util.function.Function;

public class JeroMQRemoteInvoker implements RemoteInvoker {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQRemoteInvoker.class);

    private ClientConnectionFactory clientConnectionFactory;

    @Override
    public Future<Object> invoke(final Invocation invocation,
                                 final Consumer<InvocationError> errorInvocationResultConsumer,
                                 final List<Consumer<InvocationResult>> invocationResultConsumerList) {

        try {

            final ClientConnectionFactory.Connection connection = getSocketFactory().connect();
            final ZMQ.Socket socket = connection.socket();

            sendInvocation(socket, errorInvocationResultConsumer, invocation);


        } catch (Throwable th) {
            logger.error("Error dispatching remote method invocation.");
            final InvocationError invocationError = new InvocationError();
            invocationError.setThrowable(th);
            errorInvocationResultConsumer.accept(invocationError);
        }

        return null;

    }

    private void sendInvocation(final ZMQ.Socket socket,
                                final Consumer<InvocationError> errorInvocationResultConsumer,
                                final Invocation invocation) {
    }

    public ClientConnectionFactory getSocketFactory() {
        return clientConnectionFactory;
    }

    @Inject
    public void setSocketFactory(ClientConnectionFactory socketFactory) {
        this.clientConnectionFactory = socketFactory;
    }

}
