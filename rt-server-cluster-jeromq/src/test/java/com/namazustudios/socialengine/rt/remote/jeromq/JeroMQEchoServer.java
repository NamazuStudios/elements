package com.namazustudios.socialengine.rt.remote.jeromq;

import com.namazustudios.socialengine.rt.remote.InstanceConnectionService.InstanceBinding;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;
import org.zeromq.ZMsg;

import java.util.function.Supplier;

import static java.lang.Thread.interrupted;
import static org.zeromq.SocketType.ROUTER;
import static org.zeromq.ZContext.shadow;

public class JeroMQEchoServer implements AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQEchoServer.class);

    private final ZContext shadowZContext;

    private final Supplier<InstanceBinding> instanceBindingSupplier;

    public JeroMQEchoServer(final ZContext zContext, final Supplier<InstanceBinding> instanceBindingSupplier) {
        this.shadowZContext = shadow(zContext);
        this.instanceBindingSupplier = instanceBindingSupplier;
    }

    public void run(final Runnable started) {
        try (final InstanceBinding instanceBinding = instanceBindingSupplier.get();
             final ZMQ.Socket socket = shadowZContext.createSocket(ROUTER)) {

            socket.bind(instanceBinding.getBindAddress());
            started.run();

            while (!interrupted()) {
                final ZMsg zMsg = ZMsg.recvMsg(socket);
                zMsg.send(socket);
            }

        }
    }

    @Override
    public void close() {
        shadowZContext.close();
    }

}
