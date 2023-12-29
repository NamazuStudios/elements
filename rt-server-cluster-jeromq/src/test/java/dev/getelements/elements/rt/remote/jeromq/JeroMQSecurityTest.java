package dev.getelements.elements.rt.remote.jeromq;

import dev.getelements.elements.rt.exception.InvalidPemException;
import dev.getelements.elements.rt.util.PemChain;
import org.testng.annotations.AfterClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.zeromq.SocketType;
import org.zeromq.ZContext;

import java.io.IOException;

import static java.lang.String.format;
import static org.testng.AssertJUnit.assertEquals;

public class JeroMQSecurityTest {

    private final ZContext zContext = new ZContext();

    @DataProvider
    private static Object[][] testChains() throws IOException, InvalidPemException {

        final var client = loadChain("/client_chain.pem");
        final var server = loadChain("/server_chain.pem");

        return new Object[][] {
                new Object[] { JeroMQSecurity.DEFAULT },
                new Object[] { new JeroMQCurveSecurity() },
                new Object[] { new JeroMQCurveSecurity(server) },
                new Object[] { new JeroMQCurveSecurity(server, client) }
        };

    }

    @Test(dataProvider = "testChains")
    public void testCurveSecurityChain(final JeroMQSecurity jeroMQSecurity) {
        try (var server = jeroMQSecurity.server(() -> zContext.createSocket(SocketType.REP));
             var client = jeroMQSecurity.client(() -> zContext.createSocket(SocketType.REQ))
        ) {

            server.bind(format("inproc://%s/server", JeroMQSecurityTest.class.getSimpleName()));
            client.connect(format("inproc://%s/server", JeroMQSecurityTest.class.getSimpleName()));

            client.send("Hello");
            assertEquals(server.recvStr(), "Hello");

            server.send("World!");
            assertEquals(client.recvStr(), "World!");

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
