package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.test.TestElementArtifact.MONGO;

/**
 * Regression coverage for #209, end-to-end through the real injector, runtime service, entity registrar and
 * Mongo: a <b>hot reload</b> (version bump on an enabled deployment, exactly what a re-uploaded {@code .elm}
 * triggers) must cleanly swap the deployment's entity classes.
 *
 * <p>The deployed Element ({@code sdk-test-element-mongo}) declares a Morphia entity plus an
 * {@link dev.getelements.elements.sdk.dao.EntityRegistry}, and its eager {@code TestRedeployIndexInitializer}
 * exercises the shared {@link Datastore} for that entity at Element injector-build time -- the same point
 * where the real-world failure surfaced. Pre-fix, the JVM-global entity registrar still held the first
 * generation's entity classes while the second generation's injector was being built (deregistration was
 * deferred to the container service's event-driven unmount), so the shared Mapper -- which caches entity
 * models by FQCN -- handed back first-generation models and the reload failed with
 * {@code ClassCastException: X cannot be cast to X}, silently leaving the deployment stale.
 *
 * <p>Post-fix, the runtime service deregisters the old deployment's entities synchronously during unload,
 * before the new load begins, and the reload completes cleanly with fresh entity classes.
 */
public class ElementHotReloadApiTest {

    private static final String ELEMENT_NAME = "dev.getelements.elements.sdk-test-element-mongo";

    @Inject
    @Named(ElementRegistry.ROOT)
    private MutableElementRegistry rootElementRegistry;

    private final List<String> runtimeEventLog = new CopyOnWriteArrayList<>();

    private Subscription runtimeEventSubscription;

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(ElementHotReloadApiTest.class)
        };
    }

    /**
     * The id of the deployment created by the test method, if it got that far. Cleared in the after-method
     * below once the deployment is deleted.
     */
    private String createdDeploymentId;

    @BeforeMethod
    public void recordRuntimeEvents() {
        runtimeEventLog.clear();
        runtimeEventSubscription = rootElementRegistry.onEvent(event -> {
            if (ElementRuntimeService.RUNTIME_LOADED.equals(event.getEventName())
                    || ElementRuntimeService.RUNTIME_UNLOADED.equals(event.getEventName())) {
                runtimeEventLog.add(event.getEventName());
            }
        });
    }

    @AfterMethod
    public void unsubscribeRuntimeEvents() {
        if (runtimeEventSubscription != null) {
            runtimeEventSubscription.unsubscribe();
            runtimeEventSubscription = null;
        }
    }

    /**
     * Deletes the deployment created by the test, if any, and waits for its runtime to unload. Without
     * this, the deployment (whose element declares a Morphia entity) stays registered in the JVM-global
     * {@code MongoElementEntityRegistrar} for the rest of the suite: every subsequent deployment load
     * rebuilds the shared Morphia mapper mapping this element's entity classes as well, and since Morphia
     * keeps the first model per FQCN, a later deployment declaring the same entity class name resolves the
     * entity through the stale element's classloader and crashes with
     * {@code ClassCastException: X cannot be cast to X}.
     */
    @AfterMethod
    public void deleteDeploymentAndWaitForUnload() throws InterruptedException {

        if (createdDeploymentId == null) {
            return;
        }

        try {
            elementDeploymentDao.deleteDeployment(createdDeploymentId);

            final var deadline = System.currentTimeMillis() + 60_000;

            while (System.currentTimeMillis() < deadline) {

                final var stillActive = runtimeService.getActiveRuntimes().stream()
                        .anyMatch(r -> r.deployment().id().equals(createdDeploymentId));

                if (!stillActive) {
                    createdDeploymentId = null;
                    return;
                }

                Thread.sleep(250);

            }

            Assert.fail("Deployment " + createdDeploymentId + " did not unload in time; active runtimes: "
                    + runtimeService.getActiveRuntimes());
        } finally {
            createdDeploymentId = null;
        }

    }

    @Inject
    private ElementRuntimeService runtimeService;

    @Inject
    private ElementDeploymentDao elementDeploymentDao;

    @Inject
    private LargeObjectDao largeObjectDao;

    @Inject
    private LargeObjectBucket largeObjectBucket;

    @Inject
    private Datastore datastore;

    @Test(timeOut = 120_000)
    public void hotReloadSwapsEntityClassesAndKeepsServing() throws Exception {

        // Deploy v1: an enabled deployment whose ELM LargeObject holds the MONGO test element.
        final var elm = createElmLargeObject();
        writeElmContent(elm.getId());

        final var created = elementDeploymentDao.createElementDeployment(newDeployment(elm));
        createdDeploymentId = created.id();
        final var first = awaitRuntime(created.id(), created.version(), "initial load");
        final var firstGenerationEntityClass = entityClassOf(first);

        // Hot-reload: bump the version exactly the way DeploymentElmReloader does when a new .elm is
        // uploaded to the deployment's ELM LargeObject. This drives the reconcile() reload path --
        // unload (entity deregistration + datastore rebuild) followed immediately by the new load.
        final var reloaded = elementDeploymentDao.updateElementDeployment(created);
        final var second = awaitRuntime(reloaded.id(), reloaded.version(), "hot reload");
        final var secondGenerationEntityClass = entityClassOf(second);

        Assert.assertEquals(second.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                "the hot reload must complete cleanly");

        Assert.assertNotSame(
                firstGenerationEntityClass,
                secondGenerationEntityClass,
                "a hot reload must produce a distinct Class object for the same entity name -- "
                        + "otherwise this test isn't reproducing issue #209 at all");

        Assert.assertEquals(firstGenerationEntityClass.getName(), secondGenerationEntityClass.getName(),
                "both generations must declare the same entity FQCN");

        // The reloaded element's entity must be fully usable through the shared datastore: the probe rows
        // written by each generation's eager initializer are visible, and a fresh save through the new
        // generation's class round-trips.
        Assert.assertTrue(datastore.find(secondGenerationEntityClass).count() >= 2,
                "probe rows from both generations must be readable through the shared datastore");

        final var document = secondGenerationEntityClass.getDeclaredConstructor().newInstance();
        secondGenerationEntityClass.getMethod("setId", String.class).invoke(document, "post-hot-reload");
        secondGenerationEntityClass.getMethod("setText", String.class).invoke(document, "after-hot-reload");
        datastore.save(document);

        // The unload of the old generation must be announced BEFORE the new generation loads: the
        // container service retires the old Jetty handlers at that point. Publishing the loaded event
        // first leaves the old container serving requests after the shared Morphia mapper has already
        // been rebuilt for the new generation -- every request routed to it fails with
        // ClassCastException: X cannot be cast to X (NamazuStudios/elements#209).
        final int unloadIndex = runtimeEventLog.lastIndexOf(ElementRuntimeService.RUNTIME_UNLOADED);
        final int loadedIndex = runtimeEventLog.lastIndexOf(ElementRuntimeService.RUNTIME_LOADED);
        Assert.assertTrue(unloadIndex >= 0 && loadedIndex > unloadIndex,
                "expected RUNTIME_UNLOADED before RUNTIME_LOADED; events: " + runtimeEventLog);

    }

    private ElementDeployment newDeployment(final LargeObject elm) {
        return new ElementDeployment(
                null,                                           // id -- assigned
                "hot-reload-under-test",                        // name
                null,                                           // application -- global
                LargeObjectReference.fromLargeObject(elm),      // elm
                Map.of(ELEMENT_NAME, List.of("DEFAULT")),       // pathSpiBuiltins
                Map.of(),                                       // pathSpiClassPaths
                Map.of(),                                       // pathAttributes
                List.of(),                                      // elements (deprecated)
                List.of(),                                      // packages
                true,                                           // useDefaultRepositories
                false,                                          // missionCritical
                List.of(),                                      // repositories
                ElementDeploymentState.ENABLED,                 // state
                0L                                              // version -- bumped by the DAO on update
        );
    }

    private LargeObject createElmLargeObject() {

        final var nobody = new AccessPermissions();
        nobody.setRead(Subjects.nobody());
        nobody.setWrite(Subjects.nobody());
        nobody.setDelete(Subjects.nobody());

        var elm = new LargeObject();
        elm.setPath("/system/element/test/hot-reload/deployment.elm");
        elm.setMimeType(ElementPathLoader.ELM_MIME_TYPE);
        elm.setAccessPermissions(nobody);

        return largeObjectDao.createLargeObject(elm);

    }

    private void writeElmContent(final String largeObjectId) throws IOException {
        try (final var os = largeObjectBucket.writeObject(largeObjectId);
             final var is = ElementArtifactLoader.newDefaultInstance()
                     .findArtifact(ArtifactRepository.DEFAULTS, MONGO.getCoordinatesForElm())
                     .map(Artifact::read)
                     .orElseThrow(() -> new IllegalStateException(
                             "MONGO test element ELM not found in local repository: "
                                     + MONGO.getCoordinatesForElm()))) {
            is.transferTo(os);
        }
    }

    private ElementRuntimeService.RuntimeRecord awaitRuntime(final String deploymentId,
                                                             final long version,
                                                             final String phase) throws InterruptedException {

        final var deadline = System.currentTimeMillis() + 60_000;

        while (System.currentTimeMillis() < deadline) {

            final var record = runtimeService.getActiveRuntimes()
                    .stream()
                    .filter(r -> r.deployment().id().equals(deploymentId))
                    .findFirst()
                    .orElse(null);

            if (record != null && record.deployment().version() == version) {
                Assert.assertNotEquals(record.status(), ElementRuntimeService.RuntimeStatus.FAILED,
                        phase + " failed: " + record.errors());
                return record;
            }

            Thread.sleep(250);

        }

        Assert.fail(phase + " did not reach version " + version + " in time; active runtimes: "
                + runtimeService.getActiveRuntimes());
        return null; // unreachable

    }

    private Class<?> entityClassOf(final ElementRuntimeService.RuntimeRecord runtimeRecord) {
        final var element = runtimeRecord.elements().getFirst();
        final var entityRegistry = element.getServiceLocator()
                .getInstance(dev.getelements.elements.sdk.dao.EntityRegistry.class);
        return entityRegistry.entityClasses().getFirst();
    }

}
