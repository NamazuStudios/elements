package dev.getelements.elements.deployment.jetty;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.ElementEntityRegistrar;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

/**
 * Regression coverage for #209: the hot-reload path closed the old deployment's classloaders but deferred
 * deregistration of the old deployment's Morphia entity classes to an event-driven container-service unmount
 * that runs <i>after</i> the new deployment's injector is already built. Because the shared
 * {@code Datastore}/{@code Mapper} is JVM-global and caches entity models by FQCN, the new element's injector
 * observed the old (closed) classloader's classes for identical class names, producing
 * {@code ClassCastException: X cannot be cast to X} and leaving the deployment stale until a pod restart.
 * <p>
 * These tests drive {@link StandardElementRuntimeService#reconcile()} directly with a mocked
 * {@link ElementPathLoader} so a deployment loads successfully without touching the network, and observe the
 * entity-registrar and event interactions in order.
 */
public class StandardElementRuntimeServiceReloadTest {

    private ElementDeploymentDao dao;

    private MutableElementRegistry rootRegistry;

    private ElementPathLoader pathLoader;

    private RecordingRegistrar entityRegistrar;

    private AtomicLong clock;

    private List<String> eventLog;

    @BeforeMethod
    public void setUp() {
        eventLog = new ArrayList<>();
        clock = new AtomicLong();
        entityRegistrar = new RecordingRegistrar(eventLog);
        pathLoader = mock(ElementPathLoader.class);
    }

    /**
     * On a version-bump reload, the old deployment's entity classes must be deregistered <b>before</b> the new
     * version's injector is built. Before the fix, no deregistration happened on the reload path at all — the
     * entity unmount was deferred to the (later, event-driven) container-service unmount.
     */
    @Test
    public void reloadUnregistersOldEntitiesBeforeLoadingNewVersion() {

        final var service = newService();
        final var element = mockElement("element-1");
        // A Stream is single-use: each load must get a fresh one.
        when(pathLoader.load(any(ElementPathLoader.LoadConfiguration.class)))
                .thenAnswer(invocation -> Stream.of(element));

        givenDeployments(deployment("deployment-1", 1L));
        service.reconcile();

        assertEquals(eventLog, List.of("load:start", "publish:" + ElementRuntimeService.RUNTIME_LOADED),
                "initial load must not touch the entity registrar (registration is container-driven)");

        givenDeployments(deployment("deployment-1", 2L));
        service.reconcile();

        assertEquals(
                eventLog,
                List.of("load:start",
                        "publish:" + ElementRuntimeService.RUNTIME_LOADED,
                        "unregister:element-1",
                        "publish:" + ElementRuntimeService.RUNTIME_UNLOADED,
                        "load:start",
                        "publish:" + ElementRuntimeService.RUNTIME_LOADED),
                "reload must deregister the old entities synchronously and publish the unload event "
                        + "before the new load begins (the container service retires the old serving "
                        + "handlers at the unload event, before the new load's registration rebuilds "
                        + "the shared mapper)"
        );

    }

    /**
     * A reload whose load fails must not publish {@code RUNTIME_LOADED} for the failed record. Before the fix
     * the failed record was published like a success (the container service ignores FAILED records, but other
     * consumers could not tell a failed reload from a successful one), while the log even announced
     * {@code "Reloaded deployment ... (version N -> N+1)"}.
     */
    @Test
    public void failedReloadIsNotPublishedAsRuntimeLoaded() {

        final var service = newService();
        final var element = mockElement("element-1");
        // A Stream is single-use: each load must get a fresh one.
        when(pathLoader.load(any(ElementPathLoader.LoadConfiguration.class)))
                .thenAnswer(invocation -> Stream.of(element));

        givenDeployments(deployment("deployment-1", 1L));
        service.reconcile();

        when(pathLoader.load(any(ElementPathLoader.LoadConfiguration.class)))
                .thenThrow(new IllegalStateException("boom"));

        givenDeployments(deployment("deployment-1", 2L));
        service.reconcile();

        final var loadedEvents = eventLog.stream()
                .filter(e -> e.equals("publish:" + ElementRuntimeService.RUNTIME_LOADED))
                .count();
        assertEquals(loadedEvents, 1,
                "only the initial (successful) load may publish RUNTIME_LOADED; the failed reload must not");

        assertTrue(eventLog.contains("publish:" + ElementRuntimeService.RUNTIME_UNLOADED),
                "the unload of the previous version must still be published so the container service unmounts it");

    }

    private StandardElementRuntimeService newService() {

        rootRegistry = mock(MutableElementRegistry.class);
        when(rootRegistry.newSubordinateRegistry()).thenAnswer(invocation -> {
            eventLog.add("load:start");
            return mock(MutableElementRegistry.class);
        });
        doAnswer(invocation -> {
            eventLog.add("publish:" + ((Event) invocation.getArgument(0)).getEventName());
            return null;
        }).when(rootRegistry).publish(any());

        dao = mock(ElementDeploymentDao.class);

        final var service = new StandardElementRuntimeService(mock(ElementArtifactLoader.class), pathLoader);
        service.setElementDeploymentDao(dao);
        service.setRootElementRegistry(rootRegistry);
        service.setSystemAttributes(Attributes.emptyAttributes());
        service.setPollIntervalSeconds(1);
        service.setTicker(clock::get);
        service.setElementEntityRegistrar(entityRegistrar);

        return service;

    }

    private void givenDeployments(final ElementDeployment... deployments) {
        when(dao.getElementDeploymentsByState(eq(ElementDeploymentState.ENABLED)))
                .thenReturn(List.of(deployments));
    }

    /**
     * A deployment that loads successfully without touching the network: one empty {@link ElementPathDefinition}
     * (stages a bare directory, no Maven artifacts, no ELM) served by the mocked {@link ElementPathLoader}.
     */
    private static ElementDeployment deployment(final String id, final long version) {

        final var definition = new ElementPathDefinition(
                "test-element", null, null, null, null, Map.of());

        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(id);
        when(deployment.version()).thenReturn(version);
        when(deployment.useDefaultRepositories()).thenReturn(false);
        when(deployment.repositories()).thenReturn(List.of());
        when(deployment.elm()).thenReturn(null);
        when(deployment.packages()).thenReturn(List.of());
        when(deployment.elements()).thenReturn(List.of(definition));
        return deployment;

    }

    private static Element mockElement(final String name) {

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn(name);

        final var elementRecord = mock(ElementRecord.class);
        when(elementRecord.definition()).thenReturn(definition);

        final var element = mock(Element.class);
        when(element.getElementRecord()).thenReturn(elementRecord);
        return element;

    }

    /**
     * Records register/unregister calls into the shared event log so tests can assert ordering against load
     * and publish events. Batches apply immediately (no datastore to rebuild in this harness).
     */
    private static class RecordingRegistrar implements ElementEntityRegistrar {

        private final List<String> eventLog;

        RecordingRegistrar(final List<String> eventLog) {
            this.eventLog = eventLog;
        }

        private void apply(final boolean unregister, final Element element) {
            eventLog.add((unregister ? "unregister:" : "register:")
                    + element.getElementRecord().definition().name());
        }

        @Override
        public void registerEntityClasses(final Element element) {
            apply(false, element);
        }

        @Override
        public void unregisterEntityClasses(final Element element) {
            apply(true, element);
        }

        @Override
        public Batch beginBatch() {
            return new Batch() {
                @Override
                public void registerEntityClasses(final Element element) {
                    RecordingRegistrar.this.apply(false, element);
                }

                @Override
                public void unregisterEntityClasses(final Element element) {
                    RecordingRegistrar.this.apply(true, element);
                }

                @Override
                public void close() {
                    // no datastore to rebuild in this harness
                }
            };
        }

    }

}
