package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.model.security.InvalidPemException;
import dev.getelements.elements.sdk.model.security.PemChain;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import java.io.IOException;

import static dev.getelements.elements.rt.remote.jeromq.JeroMQSecurity.DEFAULT;
import static java.lang.String.format;
import static org.testng.AssertJUnit.*;

public class JeroMQSecurityTest {

    private final ZContext zContext = new ZContext();

    private static final JeroMQSecurity GENERATED;

    private static final JeroMQSecurity PEM_PROVIDED;

    static {
        try {
            final var server = loadChain("/server_chain.pem");
            GENERATED = new JeroMQCurveSecurity();
            PEM_PROVIDED = new JeroMQCurveSecurity(server);
        } catch (IOException | InvalidPemException ex) {
            throw new InternalException(ex);
        }
    }

    @DataProvider
    private static Object[][] testAll() {
        return new Object[][] {
                new Object[] { DEFAULT },
                new Object[] { GENERATED },
                new Object[] { PEM_PROVIDED }
        };
    }

    @DataProvider
    private static Object[][] testSecure() {
        return new Object[][] {
                new Object[] { GENERATED },
                new Object[] { PEM_PROVIDED }
        };
    }

    @Test(dataProvider = "testAll")
    public void testCurveSecurityChain(final JeroMQSecurity jeroMQSecurity) {
        try (var server = jeroMQSecurity.server(() -> zContext.createSocket(SocketType.REP));
             var client = jeroMQSecurity.client(() -> zContext.createSocket(SocketType.REQ))
        ) {

            final int port = server.bindToRandomPort("tcp://*");
            final var address = format("tcp://*:%d", port);
            final var connected = client.connect(address);
            assertTrue(connected);

            client.send("Hello");
            assertEquals(server.recvStr(), "Hello");

            server.send("World!");
            assertEquals(client.recvStr(), "World!");

        }
    }

    @Test(dataProvider = "testSecure")
    public void testInsecureClientCurveSecurityFails(final JeroMQSecurity jeroMQSecurity) {
        try (var server = jeroMQSecurity.server(() -> zContext.createSocket(SocketType.REP));
             var client = DEFAULT.client(() -> zContext.createSocket(SocketType.REQ))
        ) {
            checkSecureConnectionFails(server, client);
        }
    }

    @Test(dataProvider = "testSecure")
    public void testInsecureServerCurveSecurityFails(final JeroMQSecurity jeroMQSecurity) {
        try (var server = DEFAULT.server(() -> zContext.createSocket(SocketType.REP));
             var client = jeroMQSecurity.client(() -> zContext.createSocket(SocketType.REQ))
        ) {
            checkSecureConnectionFails(server, client);
        }
    }

    @Test
    public void testUnrelatedKeysFails() {
        try (var server = new JeroMQCurveSecurity().server(() -> zContext.createSocket(SocketType.REP));
             var client = new JeroMQCurveSecurity().client(() -> zContext.createSocket(SocketType.REQ))
        ) {
            checkSecureConnectionFails(server, client);
        }
    }

    private void checkSecureConnectionFails(final ZMQ.Socket server, final ZMQ.Socket client) {

        server.setLinger(10);
        client.setLinger(10);
        server.setReceiveTimeOut(250);
        client.setReceiveTimeOut(250);

        final int port = server.bindToRandomPort("tcp://*");
        final var address = format("tcp://*:%d", port);
        client.connect(address);
        client.send("Hello");

        final var received = server.recvStr();

        if (received != null) {
            fail("Server should never have received message from unencrypted socket.");
        }

    }

    private static PemChain loadChain(final String path) throws IOException, InvalidPemException {
        try (var is = JeroMQSecurityTest.class.getResourceAsStream(path)) {
            return new PemChain(is);
        }
    }

    @AfterClass
    public void closeContext() {
        zContext.close();
    }

}
