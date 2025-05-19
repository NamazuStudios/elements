package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.model.security.PemChain;
import dev.getelements.elements.sdk.model.security.PemData;
import org.zeromq.ZMQ;

import java.util.function.Supplier;

import static dev.getelements.elements.sdk.model.security.Rfc7468Label.PRIVATE_KEY;
import static dev.getelements.elements.sdk.model.security.Rfc7468Label.PUBLIC_KEY;
import static java.lang.String.format;
import static org.zeromq.ZMQ.Curve.*;

/**
 * Implements JeroMQ CURVE Encryption using one of three possible combinations.
 *
 * {@see <a href="http://curvezmq.org/page:read-the-docs">JeroMQ CURVE Encryption</a>}
 *
 */
public class JeroMQCurveSecurity implements JeroMQSecurity {

    public static final String SOURCE_PEM = "pem";

    public static final String SOURCE_GEN = "gen";

    private final String serverSource;

    private final byte[] serverPublicKey;

    private final byte[] serverPrivateKey;

    /**
     * Creates a new {@link JeroMQSecurity} by generating the key pair on the fly. This is really only useful  for
     * testing when both client and server reside in the same memory space and the instance is shared among all
     * contexts.
     */
    public JeroMQCurveSecurity() {

        final var serverKeyPair = generateKeyPair();

        serverSource = SOURCE_GEN;

        serverPublicKey = z85Decode(serverKeyPair.publicKey);
        serverPrivateKey = z85Decode(serverKeyPair.secretKey);

    }

    /**
     * Creates a new {@link JeroMQSecurity} with the supplied server key chain.
     *
     * @param server
     */
    public JeroMQCurveSecurity(final PemChain server) {

        serverPublicKey = server
                .findFirstWithLabel(PUBLIC_KEY)
                .map(PemData::getSpec)
                .map(byte[]::clone)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PUBLIC_KEY.getLabel())));

        serverPrivateKey = server
                .findFirstWithLabel(PRIVATE_KEY)
                .map(PemData::getSpec)
                .map(byte[]::clone)
                .orElseThrow(() -> new InternalException(format("No %s in JeroMQ Server Security Chain", PRIVATE_KEY.getLabel())));

        serverSource = SOURCE_PEM;

    }

    @Override
    public ZMQ.Socket server(final Supplier<ZMQ.Socket> socketSupplier) {

        final var socket = socketSupplier.get();

        if (!socket.setCurveServer(true)) {
            throw new InternalException("Unable to enable CURVE security on socket.");
        }

        if (!socket.setCurveSecretKey(serverPrivateKey)) {
            throw new InternalException("Unable to assign CURVE private key to server.");
        }

        return socket;

    }

    @Override
    public ZMQ.Socket client(final Supplier<ZMQ.Socket> socketSupplier) {

        final var socket = socketSupplier.get();
        final var clientKeyPair = generateKeyPair();

        final var clientPublicKey = z85Decode(clientKeyPair.publicKey);
        final var clientPrivateKey = z85Decode(clientKeyPair.secretKey);

        if (!socket.setCurveServerKey(serverPublicKey)) {
            throw new InternalException("Unable to assign CURVE server key to client.");
        }

        if (!socket.setCurvePublicKey(clientPublicKey)) {
            throw new InternalException("Unable to assign CURVE public key to client.");
        }

        if (!socket.setCurveSecretKey(clientPrivateKey)) {
            throw new InternalException("Unable to assign CURVE private key to client.");
        }

        return socket;

    }

    @Override
    public String toString() {
        return "JeroMQCurveSecurityChain{" +
                "serverPublicKey=" + z85Encode(serverPublicKey) +
                ", serverPrivateKey=<redacted>" +
                ", serverSource=" + serverSource +
                '}';
    }

}
