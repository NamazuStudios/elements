package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.rt.util.PemChain;
import dev.getelements.elements.rt.util.PemData;
import org.zeromq.ZMQ;

import java.util.Arrays;
import java.util.Base64;
import java.util.function.Supplier;

import static dev.getelements.elements.rt.util.Rfc7468Label.PRIVATE_KEY;
import static dev.getelements.elements.rt.util.Rfc7468Label.PUBLIC_KEY;
import static java.lang.String.format;
import static org.zeromq.ZMQ.Curve.*;

public class JeroMQCurveSecurityChain implements JeroMQSecurityChain {

    private final byte[] clientPublicKey;

    private final byte[] clientPrivateKey;

    private final byte[] serverPublicKey;

    private final byte[] serverPrivateKey;

    /**
     * Creates a new {@link JeroMQSecurityChain} with the supplied server key chain.
     *
     * @param server
     */
    public JeroMQCurveSecurityChain(final PemChain server) {

        serverPublicKey = server
                .findFirstWithLabel(PUBLIC_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PUBLIC_KEY.getLabel())));

        serverPrivateKey = server
                .findFirstWithLabel(PRIVATE_KEY)
                .map(PemData::getSpec)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PRIVATE_KEY.getLabel())));

        final var clientKeyPair = generateKeyPair();

        clientPublicKey = z85Decode(clientKeyPair.publicKey);
        clientPrivateKey = z85Decode(clientKeyPair.secretKey);

    }

    public JeroMQCurveSecurityChain(final PemChain server, final PemChain client) {

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
    public ZMQ.Socket client(final Supplier<ZMQ.Socket> socketSupplier) {
        final var socket = socketSupplier.get();
        socket.setCurveServerKey(serverPublicKey);
        socket.setCurvePublicKey(clientPublicKey);
        socket.setCurveSecretKey(clientPrivateKey);
        return socket;
    }

    @Override
    public ZMQ.Socket server(final Supplier<ZMQ.Socket> socketSupplier) {
        final var socket = socketSupplier.get();
        socket.setCurveServer(true);
        socket.setCurvePublicKey(serverPublicKey);
        socket.setCurveSecretKey(serverPrivateKey);
        return socket;
    }

    @Override
    public String toString() {
        return "JeroMQCurveSecurityChain{" +
                "clientPublicKey=" + z85Encode(clientPublicKey) +
                ", clientPrivateKey=<redacted>" +
                ", serverPublicKey=" + z85Encode(serverPublicKey) +
                ", serverPrivateKey=<redacted>" +
                '}';
    }

}
