package dev.getelements.elements.rest.test;

import com.google.inject.Injector;
import dev.getelements.elements.sdk.transact.JournalTransactionalResourceServicePersistenceEnvironment;
import dev.getelements.elements.sdk.transact.unixfs.UnixFSJournalTransactionalPersistenceDriver;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.core.Response;

import java.time.Duration;

import static com.google.inject.Guice.createInjector;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

public class TestUtils {

    public static final String TEST_INSTANCE = "dev.getelements.elements.test.instance";

    public static final String TEST_API_ROOT = "dev.getelements.elements.rest.test.api.root";

    public static final String TEST_APP_SERVE_RS_ROOT = "dev.getelements.elements.rest.test.appserve.rs.root";

    public static final String TEST_APP_SERVE_WS_ROOT = "dev.getelements.elements.rest.test.appserve.ws.root";

    private static final TestUtils instance = new TestUtils();

    public static TestUtils getInstance() {
        return instance;
    }

    private final Injector unixFSInjector;

    private TestUtils() {
        final var module = new EmbeddedRestApiIntegrationTestModule();
        unixFSInjector = createInjector(module);
    }

    /**
     * Creates a test case with the {@link JournalTransactionalResourceServicePersistenceEnvironment} backed by the
     * {@link UnixFSJournalTransactionalPersistenceDriver}
     *
     * @param testClass the type to construct
     * @param <T> the type to return
     * @return the constructed test case
     */
    public <T> T getTestFixture(final Class<T> testClass) {
        return unixFSInjector.getInstance(testClass);
    }

    /**
     * Polls the given URL until it returns HTTP 200, or fails after 30 seconds.
     *
     * <p>{@code JakartaRsLoader} builds the JAX-RS handler for an Element on a background
     * {@code mountExecutor}; {@link dev.getelements.elements.sdk.deployment.ElementRuntimeService#loadTransientDeployment}
     * returns before Jersey finishes initialising. Until that completes, the un-started
     * {@code ServletContextHandler} returns {@code false} from {@code Handler#handle} and
     * requests fall through the handler sequence to the main Guice servlet context, which
     * 405s on POST (and returns a JSON body shaped like {@code {"status":"405","url":...}}).
     *
     * <p>Integration tests that deploy an Element should call this method in {@code @BeforeClass}
     * after {@code loadTransientDeployment} with a known-good readiness URL — typically
     * {@code <appServeRoot>/<prefix>/openapi.json} for REST elements.
     *
     * @param client       the JAX-RS client to poll with
     * @param readinessUrl a URL that returns 200 once the Element's handler is mounted
     * @throws IllegalStateException if readiness is not reached within 30 seconds
     */
    public static void awaitElementReady(final Client client, final String readinessUrl) {
        final long deadline = System.nanoTime() + Duration.ofSeconds(30).toNanos();
        Response last = null;
        while (System.nanoTime() < deadline) {
            try {
                last = client.target(readinessUrl).request(APPLICATION_JSON).get();
                if (last.getStatus() == 200) {
                    last.close();
                    return;
                }
                last.close();
            } catch (final Exception ignored) {
                // Connection errors during boot — keep polling.
            }
            try {
                Thread.sleep(100);
            } catch (final InterruptedException e) {
                Thread.currentThread().interrupt();
                throw new IllegalStateException("Interrupted while waiting for Element to become ready");
            }
        }
        throw new IllegalStateException("Element JAX-RS handler at " + readinessUrl
                + " did not become ready within 30s (last status="
                + (last == null ? "<no response>" : String.valueOf(last.getStatus())) + ")");
    }

}
