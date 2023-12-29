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

public class JeroMQCurveSecurityChainTest {

    private final ZContext zContext = new ZContext();

    @DataProvider
    private static Object[][] testChains() throws IOException, InvalidPemException {

        final var client = loadChain("/client_chain.pem");
        final var server = loadChain("/server_chain.pem");

        return new Object[][] {
                new Object[] { JeroMQSecurityChain.DEFAULT },
                new Object[] { new JeroMQCurveSecurityChain(server) },
                new Object[] { new JeroMQCurveSecurityChain(server, client) }
        };

    }

    @Test(dataProvider = "testChains")
    public void testCurveSecurityChain(final JeroMQSecurityChain jeroMQSecurityChain) {
        try (var server = jeroMQSecurityChain.server(() -> zContext.createSocket(SocketType.REP));
             var client = jeroMQSecurityChain.client(() -> zContext.createSocket(SocketType.REQ))
        ) {

            server.bind(format("inproc://%s/server", JeroMQCurveSecurityChainTest.class.getSimpleName()));
            client.connect(format("inproc://%s/server", JeroMQCurveSecurityChainTest.class.getSimpleName()));

            client.send("Hello");
            assertEquals(server.recvStr(), "Hello");

            server.send("World!");
            assertEquals(client.recvStr(), "World!");

        }
    }

    private static PemChain loadChain(final String path) throws IOException, InvalidPemException {
        try (var is = JeroMQCurveSecurityChainTest.class.getResourceAsStream(path)) {
            return new PemChain(is);
        }
    }

    @AfterClass
    public void closeContext() {
        zContext.close();
    }

}
