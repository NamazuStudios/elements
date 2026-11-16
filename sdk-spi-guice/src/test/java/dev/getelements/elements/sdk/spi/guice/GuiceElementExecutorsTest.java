package dev.getelements.elements.sdk.spi.guice;

import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementExecutors;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.ServiceLocator;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.concurrent.atomic.AtomicReference;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertTrue;

/**
 * Covers the platform-managed executor facility (#209): executors vended through {@link ElementExecutors}
 * are thread-named per element and executor, and are shut down when the owning {@link Element} closes --
 * the mechanism that lets Element background work die with its Element generation instead of surviving a
 * hot reload as a zombie executor.
 */
public class GuiceElementExecutorsTest {

    private ElementRecord elementRecord;

    private GuiceElementExecutors elementExecutors;

    @BeforeMethod
    public void setUp() {

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn("dev.getelements.test.element");

        elementRecord = mock(ElementRecord.class);
        when(elementRecord.definition()).thenReturn(definition);

        final var serviceLocator = mock(ServiceLocator.class);
        final var elementRegistry = mock(ElementRegistry.class);
        element = new GuiceSdkElement(elementRecord, serviceLocator, elementRegistry);

        elementExecutors = new GuiceElementExecutors(elementRecord, element);

    }

    private GuiceSdkElement element;

    @Test
    public void threadsAreNamedPerElementAndExecutor() {

        final var poller = elementExecutors.newScheduledExecutor("metering-sync", 2);

        final var observedName = new AtomicReference<String>();
        poller.submit(() -> observedName.set(Thread.currentThread().getName()));

        final var deadline = System.currentTimeMillis() + 5000;
        while (observedName.get() == null && System.currentTimeMillis() < deadline) {
            Thread.onSpinWait();
        }

        assertTrue(observedName.get().startsWith("dev.getelements.test.element:metering-sync-"),
                "threads must be named <element>:<executor>-<n>; got: " + observedName.get());

    }

    @Test
    public void closeShutsDownAllTrackedExecutors() {

        final var one = elementExecutors.newScheduledExecutor("one", 1);
        final var two = elementExecutors.newExecutor("two");

        elementExecutors.close();

        assertTrue(one.isShutdown(), "tracked executors must be shut down when the element closes");
        assertTrue(two.isShutdown(), "tracked executors must be shut down when the element closes");

    }

    @Test
    public void closeIsIdempotent() {
        final var executor = elementExecutors.newScheduledExecutor("one", 1);
        elementExecutors.close();
        elementExecutors.close(); // must not throw or double-reap
        assertTrue(executor.isShutdown());
    }

    /**
     * The integration that matters: the facility registers its reaper via {@code element.onClose}, so
     * {@link GuiceSdkElement#close()} reaps the element's vended executors BEFORE the element's classloader
     * closes, and a poller task racing the shutdown still sees open archives.
     */
    @Test
    public void elementCloseReapsVendedExecutors() {

        final var executor = elementExecutors.newScheduledExecutor("poller", 1);

        element.close();

        assertTrue(executor.isShutdown(),
                "the element's close must reap executors vended through the facility");

    }

    /**
     * An element that never vends an executor must close cleanly (the reaper is a no-op).
     */
    @Test
    public void elementCloseToleratesUnusedFacility() {
        element.close(); // must not throw with no vended executors
    }

}
