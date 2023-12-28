package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.util.PemChain;
import dev.getelements.elements.rt.util.PemData;
import org.zeromq.ZMQ;

import static dev.getelements.elements.rt.util.Rfc7468Label.PRIVATE_KEY;
import static dev.getelements.elements.rt.util.Rfc7468Label.PUBLIC_KEY;
import static java.lang.String.format;

public class JeroMQCurveSecurityChain implements JeroMQSecurityChain {

    private final byte[] clientPublicKey;

    private final byte[] clientPrivateKey;

    private final byte[] serverPublicKey;

    private final byte[] serverPrivateKey;

    public JeroMQCurveSecurityChain(final PemChain server, final PemChain client) {

        if (server != client) {
            throw new InternalException(
                    "Must specify both client and server security chains or none at all: " +
                            "Server Configured: " + (server != null) +
                            "Client Configured: " + (client != null)
            );
        }

        serverPublicKey = server
                .findFirstWithLabel(PUBLIC_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PUBLIC_KEY.getLabel())));

        serverPrivateKey = server
                .findFirstWithLabel(PRIVATE_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PRIVATE_KEY.getLabel())));

        clientPublicKey = client
                .findFirstWithLabel(PUBLIC_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Client Security Chain", PUBLIC_KEY.getLabel())));

        clientPrivateKey = client
                .findFirstWithLabel(PRIVATE_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Client Security Chain", PRIVATE_KEY.getLabel())));

    }

    @Override
    public ZMQ.Socket client(final ZMQ.Socket socket) {
        socket.setCurveServerKey(serverPublicKey);
        socket.setCurvePublicKey(clientPublicKey);
        socket.setCurveSecretKey(clientPrivateKey);
        return socket;
    }

    @Override
    public ZMQ.Socket server(final ZMQ.Socket socket) {
        socket.setCurveServer(true);
        socket.setCurvePublicKey(serverPublicKey);
        socket.setCurveSecretKey(serverPrivateKey);
        return socket;
    }

}
