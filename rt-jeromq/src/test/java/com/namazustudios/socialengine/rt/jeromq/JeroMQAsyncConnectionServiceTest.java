package com.namazustudios.socialengine.rt.jeromq;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.rt.AsyncConnectionPool;
import com.namazustudios.socialengine.rt.AsyncConnectionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;
import org.zeromq.ZContext;
import org.zeromq.ZMQ;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

import static com.namazustudios.socialengine.rt.AsyncConnection.Event.*;
import static java.lang.Thread.interrupted;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.ConcurrentHashMap.newKeySet;
import static org.testng.Assert.assertEquals;
import static org.zeromq.SocketType.REP;
import static org.zeromq.SocketType.REQ;
import static org.zeromq.ZContext.shadow;

@Guice(modules = JeroMQAsyncConnectionServiceTest.Module.class)
public class JeroMQAsyncConnectionServiceTest {

    private static final Logger logger = LoggerFactory.getLogger(JeroMQAsyncConnectionServiceTest.class);

    private static final int POOL_COUNT = 10;

    private static final String TEST_URL = "inproc://simple-socket-manager-test";

    private ZContext zContext;

    private AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService;

    private Thread mockServer;

    private List<AsyncConnectionPool<ZContext, ZMQ.Socket>> managedPoolList;

    private final Set<String> sent = newKeySet();

    private final Set<String> received = newKeySet();

    @DataProvider
    private Object[][] getManagedPools() {
        return managedPoolList.stream().map(p -> new Object[]{p}).toArray(Object[][]::new);
    }

    @BeforeClass
    public void setupSimpleServer() {
        mockServer = new Thread(this::runMockServer);
        mockServer.setName(JeroMQAsyncConnectionServiceTest.class.getSimpleName() + " mock server.");
        mockServer.setUncaughtExceptionHandler(((t, e) -> logger.error("Caught exception {}", t, e)));
        mockServer.setDaemon(true);
        mockServer.start();
    }

    @AfterClass
    public void shutdownSimpleServer() throws Exception {
        mockServer.interrupt();
        mockServer.join();
    }

    private void runMockServer() {
        try (final ZContext shadow = shadow(getzContext());
             final ZMQ.Socket socket = shadow.createSocket(REP)) {

            socket.setLinger(1000);
            socket.setReceiveTimeOut(1000);
            socket.bind(TEST_URL);

            while (!interrupted()) {
                final String msg = socket.recvStr();
                if (msg != null) socket.send(received.add(msg) ? msg : "error - " + msg);
            }

        }
    }

    @BeforeClass
    public void startService() {
        getAsyncConnectionService().start();
        managedPoolList = new ArrayList<>();
    }

    @AfterClass(dependsOnMethods = "destroyManagedPools")
    public void stopService() {
        getAsyncConnectionService().stop();
    }

    @BeforeClass(dependsOnMethods = "startService")
    public void acquireManagedPools() {
        for (int i = 0; i < POOL_COUNT; ++i) {
            final AsyncConnectionPool<ZContext, ZMQ.Socket> managedPool = getAsyncConnectionService().allocatePool(
                "TestPool: " + (i+1),
                20,
                1000,
                z -> {
                    final ZMQ.Socket socket = z.createSocket(REQ);
                    socket.connect(TEST_URL);
                    return socket;
                });

            managedPoolList.add(managedPool);
        }
    }

    @AfterClass
    public void destroyManagedPools() {
        managedPoolList.forEach(p -> p.close());
    }

    @Test(dataProvider = "getManagedPools", invocationCount = 100, threadPoolSize = 25)
    public void testPool(final AsyncConnectionPool<ZContext, ZMQ.Socket> managedPool) throws InterruptedException {

        final String msg = randomUUID().toString();
        final CountDownLatch latch = new CountDownLatch(2);

        final AtomicInteger errno = new AtomicInteger();
        final AtomicReference<String> response = new AtomicReference<>();

        managedPool.acquireNextAvailableConnection(c -> {

            c.onWrite(c0 -> {
                sent.add(msg);
                c.socket().send(msg);
                latch.countDown();
                c.setEvents(READ, ERROR);
            });

            c.onError(c0 -> {
                errno.set(c.socket().errno());
                latch.countDown();
                c.close();
            });

            c.onRead(c0 -> {
                response.set(c.socket().recvStr());
                latch.countDown();
                c.recycle();
            });

            c.setEvents(READ, WRITE, ERROR);

        });

        latch.await();
        assertEquals(response.get(), msg);
        assertEquals(errno.get(), 0);

    }

    @Test(dependsOnMethods = "testPool")
    public void verifyPostconditions() {
        assertEquals(sent, received);
    }

    public ZContext getzContext() {
        return zContext;
    }

    @Inject
    public void setzContext(ZContext zContext) {
        this.zContext = zContext;
    }

    public AsyncConnectionService<ZContext, ZMQ.Socket> getAsyncConnectionService() {
        return asyncConnectionService;
    }

    @Inject
    public void setAsyncConnectionService(AsyncConnectionService<ZContext, ZMQ.Socket> asyncConnectionService) {
        this.asyncConnectionService = asyncConnectionService;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            bind(ZContext.class).toProvider(() -> {
                final ZContext zContext = new ZContext();
                zContext.getContext().setMaxSockets(1024 * 8);
                return zContext;
            }).asEagerSingleton();

            bind(new TypeLiteral<AsyncConnectionService<ZContext, ZMQ.Socket>>(){})
                .to(JeroMQAsyncConnectionService.class).asEagerSingleton();

        }
    }

}
