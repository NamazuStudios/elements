package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.Subscription;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.deployment.ElementContainerService;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.record.Artifact;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.test.TestElementArtifact;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.ws.rs.client.Client;
import org.testng.Assert;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import static dev.getelements.elements.rest.test.TestUtils.TEST_APP_SERVE_RS_ROOT;
import static dev.getelements.elements.sdk.test.TestElementArtifact.MONGO;
import static dev.getelements.elements.sdk.test.TestElementArtifact.JAKARTA_RS;
import static jakarta.ws.rs.core.MediaType.APPLICATION_JSON;

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
     * The ids of the deployments created by the test methods, if they got that far. Cleared in the
     * after-method below once the deployments are deleted.
     */
    private final List<String> createdDeploymentIds = new CopyOnWriteArrayList<>();

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

        if (createdDeploymentIds.isEmpty()) {
            return;
        }

        try {

            for (final var id : createdDeploymentIds) {
                elementDeploymentDao.deleteDeployment(id);
            }

            final var deadline = System.currentTimeMillis() + 60_000;

            while (System.currentTimeMillis() < deadline) {

                final var stillActive = runtimeService.getActiveRuntimes().stream()
                        .anyMatch(r -> createdDeploymentIds.contains(r.deployment().id()));

                if (!stillActive) {
                    createdDeploymentIds.clear();
                    return;
                }

                Thread.sleep(250);

            }

            Assert.fail("Deployments " + createdDeploymentIds + " did not unload in time; active runtimes: "
                    + runtimeService.getActiveRuntimes());
        } finally {
            createdDeploymentIds.clear();
        }

    }

    @Inject
    private ElementRuntimeService runtimeService;

    @Inject
    private ElementDeploymentDao elementDeploymentDao;

    @Inject
    private ApplicationDao applicationDao;

    @Inject
    private Client client;

    @Inject
    @Named(TEST_APP_SERVE_RS_ROOT)
    private String appServeRoot;

    /**
     * The current persisted record of the HTTP round-trip test's deployment (version-bumped on each swap).
     */
    private ElementDeployment liveHttpDeployment;

    @Inject
    private LargeObjectDao largeObjectDao;

    @Inject
    private LargeObjectBucket largeObjectBucket;

    @Inject
    private Datastore datastore;

    @Test(timeOut = 300_000)
    public void hotReloadSwapsEntityClassesAndKeepsServing() throws Exception {

        // Deploy v1: an enabled deployment whose ELM LargeObject holds the MONGO test element.
        final var elm = createElmLargeObject();
        writeElmContent(elm.getId());

        final var created = elementDeploymentDao.createElementDeployment(newDeployment(elm));
        createdDeploymentIds.add(created.id());
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

        // A SECOND hot-swap must be just as clean as the first: repeated swaps must not accumulate
        // classloader generations whose entity classes shadow each other in the shared mapper
        // (staging reproduced CCEs on the second swap of a production element, 2026-11-13).
        final var reloadedAgain = elementDeploymentDao.updateElementDeployment(reloaded);
        final var third = awaitRuntime(reloadedAgain.id(), reloadedAgain.version(), "second hot reload");
        final var thirdGenerationEntityClass = entityClassOf(third);

        Assert.assertEquals(third.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                "the second hot reload must complete cleanly");

        Assert.assertNotSame(secondGenerationEntityClass, thirdGenerationEntityClass,
                "each reload must produce a distinct Class object");

        final var secondReloadUnloadIndex = runtimeEventLog.lastIndexOf(ElementRuntimeService.RUNTIME_UNLOADED);
        Assert.assertTrue(secondReloadUnloadIndex > loadedIndex,
                "the second reload must announce the unload of the previous generation; events: "
                        + runtimeEventLog);

        // The unload path must eventually close the OLD generation's element -- firing the onClose hooks
        // Element authors use to stop background executors (Fix C). The close is deferred by the unload
        // grace period (default 30s), so poll until it fires. The flag is read from the OLD generation's
        // still-loaded class: each generation has its own classloader and therefore its own static.
        assertOnCloseFired(firstGenerationEntityClass, "first generation");
        assertOnCloseFired(secondGenerationEntityClass, "second generation");

    }

    /**
     * Waits (bounded) for the given element generation's {@code TestRedeployIndexInitializer.ON_CLOSE_FIRED}
     * flag to flip -- proving the platform closed that generation's element and fired its close hooks on the
     * hot-reload unload path. Reading the static of an already-loaded class requires no classloading, so
     * this works even after the generation's classloader is closed.
     */
    private static void assertOnCloseFired(final Class<?> generationEntityClass, final String label)
            throws Exception {

        final var initializerClass = Class.forName(
                "dev.getelements.elements.sdk.test.element.mongo.TestRedeployIndexInitializer",
                false,
                generationEntityClass.getClassLoader());

        final var flag = (java.util.concurrent.atomic.AtomicBoolean)
                initializerClass.getField("ON_CLOSE_FIRED").get(null);

        final var deadline = System.currentTimeMillis() + 60_000;
        while (System.currentTimeMillis() < deadline && !flag.get()) {
            Thread.sleep(500);
        }

        Assert.assertTrue(flag.get(),
                "the " + label + " element's onClose hook must fire after its hot-reload unload "
                        + "(the grace-period close) -- Element authors rely on it to stop background executors");

    }

    /**
     * Four deployments coexist, one per attribute shape: name only, application only, both, neither. Each
     * carries the same entity-declaring Element and is hot-swapped <b>in turn</b> while the other three stay
     * live. Every swap must complete cleanly with a fresh entity {@link Class} object, and the untouched
     * deployments must remain active and CLEAN: a swap rebuilds the JVM-global shared Morphia mapper for
     * <i>all</i> registered elements, so a swap that poisons the registrar's bookkeeping would break its
     * neighbors too (NamazuStudios/elements#209).
     */
    @Test(timeOut = 300_000)
    public void fourDeploymentsWithAttributeMatrixHotSwapOneByOne() throws Exception {

        // One application fixture, shared by the application-scoped deployments.
        final var application = new Application();
        application.setName("HRMT4APP");
        application.setDescription("Hot-reload four-deployment matrix test application");
        application.setMaxProfiles(1000);
        final var createdApplication = applicationDao.createOrUpdateInactiveApplication(application);

        // Each deployment stages its own ELM LargeObject (same MONGO test element artifact).
        final var live = new ArrayList<ElementDeployment>();

        final var matrix = new ArrayList<ElementDeployment>();
        matrix.add(newDeployment(createElmLargeObjectWithContent(), "hot-reload-matrix-name-only", null));
        matrix.add(newDeployment(createElmLargeObjectWithContent(), null, createdApplication));
        matrix.add(newDeployment(createElmLargeObjectWithContent(), "hot-reload-matrix-both", createdApplication));
        matrix.add(newDeployment(createElmLargeObjectWithContent(), null, null));

        for (final var request : matrix) {
            live.add(elementDeploymentDao.createElementDeployment(request));
        }

        createdDeploymentIds.addAll(live.stream().map(ElementDeployment::id).toList());

        // All four load concurrently on the next reconcile; every runtime must come up CLEAN.
        final var currentEntityClasses = new java.util.HashMap<String, Class<?>>();

        for (final var deployment : live) {
            final var record = awaitRuntime(deployment.id(), deployment.version(),
                    "initial load of " + describe(deployment));
            Assert.assertEquals(record.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                    "the initial load of " + describe(deployment) + " must complete cleanly");
            currentEntityClasses.put(deployment.id(), entityClassOf(record));
        }

        // Hot-swap each deployment in turn. After each swap, the swapped deployment must serve a fresh
        // entity Class, and the other three must still be active, CLEAN, and at their unchanged versions.
        for (int i = 0; i < live.size(); i++) {

            final var before = live.get(i);
            final var swapped = elementDeploymentDao.updateElementDeployment(before);
            live.set(i, swapped);

            final var label = describe(before);

            final var record = awaitRuntime(swapped.id(), swapped.version(), "hot reload of " + label);

            Assert.assertEquals(record.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                    "the hot reload of " + label + " must complete cleanly");

            final var newEntityClass = entityClassOf(record);

            Assert.assertNotSame(currentEntityClasses.get(swapped.id()), newEntityClass,
                    "the hot reload of " + label + " must produce a distinct entity Class object");

            currentEntityClasses.put(swapped.id(), newEntityClass);

            final int unloadIndex = runtimeEventLog.lastIndexOf(ElementRuntimeService.RUNTIME_UNLOADED);
            final int loadedIndex = runtimeEventLog.lastIndexOf(ElementRuntimeService.RUNTIME_LOADED);
            Assert.assertTrue(unloadIndex >= 0 && loadedIndex > unloadIndex,
                    "the swap of " + label + " must announce RUNTIME_UNLOADED before RUNTIME_LOADED; events: "
                            + runtimeEventLog);

            for (int j = 0; j < live.size(); j++) {
                if (j == i) {
                    continue;
                }
                final var other = live.get(j);
                final var otherRecord = runtimeService.getActiveRuntimes().stream()
                        .filter(r -> r.deployment().id().equals(other.id()))
                        .findFirst()
                        .orElse(null);
                Assert.assertNotNull(otherRecord,
                        describe(other) + " must stay active while " + label + " hot-swaps");
                Assert.assertEquals(otherRecord.deployment().version(), other.version(),
                        describe(other) + " must not reload while " + label + " hot-swaps");
                Assert.assertEquals(otherRecord.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                        describe(other) + " must stay CLEAN while " + label + " hot-swaps; errors: "
                                + otherRecord.errors());
            }

        }

    }

    /**
     * A human-readable description of a deployment for assertion messages, in terms of the attribute matrix
     * (deployment name and/or application, or neither).
     */
    private static String describe(final ElementDeployment deployment) {
        final var hasName = deployment.name() != null;
        final var hasApplication = deployment.application() != null;
        if (hasName && hasApplication) return "the named application-scoped deployment '" + deployment.name() + "'";
        if (hasName) return "the named deployment '" + deployment.name() + "'";
        if (hasApplication) return "the unnamed application-scoped deployment " + deployment.id();
        return "the unnamed system-wide deployment " + deployment.id();
    }

    /**
     * The rc-8 staging signature (2026-11-14), end-to-end over HTTP: during a hot swap, the OLD generation's
     * Jetty handlers were left mounted while the new version loaded (minutes in staging) -- but the old
     * elements' classloaders were already closed, so requests routed to the stale handlers failed with
     * {@code NoClassDefFoundError} against the closed archives. Post-fix, the unload event retires the stale
     * handlers immediately (version-aware unmount) and the route serves only clean statuses across the whole
     * swap window: 200 while the old generation is still mounted, 404 while the route is retired, and 200
     * again once the new generation mounts -- never 5xx.
     */
    @Test(timeOut = 300_000)
    public void hotSwapNeverServesRequestsFromClosedClassloaders() throws Exception {

        final var elm = createElmLargeObjectWithContent(JAKARTA_RS);
        liveHttpDeployment = elementDeploymentDao.createElementDeployment(newRsDeployment(elm));
        createdDeploymentIds.add(liveHttpDeployment.id());

        final var route = appServeRoot + "/hotreload/message";

        // Boot load, then wait for the route to come up.
        final var first = awaitRuntime(liveHttpDeployment.id(), liveHttpDeployment.version(), "initial load");
        Assert.assertEquals(first.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                "the initial load must complete cleanly; warnings: " + first.warnings()
                        + "; errors: " + first.errors() + "; logs: " + first.logs());

        awaitRouteServing(route, "the element route must serve before the first hot swap");

        // TWO hot swaps against the same route: staging degraded on repeated swaps. Each swap's window
        // must serve only clean statuses (200 from a live generation, 404 while the route is retired)
        // and the fresh generation must serve once the swap completes.
        for (int swap = 1; swap <= 2; swap++) {

            final var before = liveHttpDeployment;
            final var reloaded = elementDeploymentDao.updateElementDeployment(before);
            liveHttpDeployment = reloaded;

            final var statusesDuringWindow = new CopyOnWriteArrayList<Integer>();
            final var polling = new AtomicBoolean(true);
            final var poller = new Thread(() -> {
                while (polling.get()) {
                    try {
                        statusesDuringWindow.add(client.target(route).request(APPLICATION_JSON).get().getStatus());
                    } catch (Exception ex) {
                        statusesDuringWindow.add(-1); // connector-level failure -- recorded, never expected
                    }
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException ex) {
                        return;
                    }
                }
            });
            poller.setDaemon(true);
            poller.start();

            try {
                final var record = awaitRuntime(reloaded.id(), reloaded.version(), "hot swap " + swap);
                Assert.assertEquals(record.status(), ElementRuntimeService.RuntimeStatus.CLEAN,
                        "hot swap " + swap + " must complete cleanly");
            } finally {
                polling.set(false);
                poller.join(5000);
            }

            // The staging failure signature was 5xx from stale handlers serving closed archives: a 5xx at
            // ANY point during the swap window means requests routed to (or resolved against) a retired
            // generation.
            final var serverErrors = statusesDuringWindow.stream().filter(s -> s >= 500).toList();
            Assert.assertEquals(serverErrors, List.of(),
                    "no request during swap " + swap + " may return 5xx; statuses observed: "
                            + statusesDuringWindow);

            final var connectorFailures = statusesDuringWindow.stream().filter(s -> s == -1).toList();
            Assert.assertEquals(connectorFailures, List.of(),
                    "no request during swap " + swap + " may fail at the connector; statuses observed: "
                            + statusesDuringWindow);

            // The fresh generation must serve. The mount happens in the RUNTIME_LOADED event handler,
            // which can lag the runtime record flipping to CLEAN -- so poll rather than assert instantly.
            awaitRouteServing(route, "the route must serve again once hot swap " + swap
                    + " mounts the new generation");

        }

    }

    /**
     * Polls the element route until it serves 200, failing with the offending status and response body
     * otherwise.
     */
    private void awaitRouteServing(final String route, final String message) throws InterruptedException {

        final var deadline = System.currentTimeMillis() + 60_000;
        var lastBody = "<none>";
        var lastStatus = -1;

        while (System.currentTimeMillis() < deadline && lastStatus != 200) {
            final var response = client.target(route).request(APPLICATION_JSON).get();
            lastStatus = response.getStatus();
            if (lastStatus != 200) {
                try {
                    response.bufferEntity();
                    lastBody = response.readEntity(String.class);
                } catch (Exception ex) {
                    lastBody = "<unreadable: " + ex + ">";
                }
            }
            Thread.sleep(100);
        }

        if (lastStatus != 200) {
            Assert.fail(message + "; last status " + lastStatus + "; body: " + lastBody);
        }

    }

    /**
     * The ELM-internal path of the {@code JAKARTA_RS} test element (the archive's element directory name),
     * used as the key for per-path SPI and attribute mappings.
     */
    private static final String RS_ELEMENT_PATH = "/dev.getelements.elements.sdk-test-element-rs";

    /**
     * A persistent deployment of the {@code JAKARTA_RS} test element, with its REST route pinned to
     * {@code /app/rest/hotreload} (overriding the element's bundled default prefix via the deployment's
     * path attributes) so the route cannot collide with other suite fixtures. The element's own manifest
     * declares its builtin SPI, so no explicit SPI mapping is needed.
     */
    private ElementDeployment newRsDeployment(final LargeObject elm) {
        return new ElementDeployment(
                null,                                           // id -- assigned
                "hot-reload-http-under-test",                   // name
                null,                                           // application -- global
                LargeObjectReference.fromLargeObject(elm),      // elm
                Map.of(),                                       // pathSpiBuiltins (manifest-declared)
                Map.of(),                                       // pathSpiClassPaths
                Map.of(RS_ELEMENT_PATH,
                        Map.of(ElementContainerService.APPLICATION_PREFIX, "hotreload")), // pathAttributes
                List.of(),                                      // elements (deprecated)
                List.of(),                                      // packages
                true,                                           // useDefaultRepositories
                false,                                          // missionCritical
                List.of(),                                      // repositories
                ElementDeploymentState.ENABLED,                 // state
                0L                                              // version -- bumped by the DAO on update
        );
    }

    /**
     * A fresh ELM LargeObject with the MONGO test element's ELM content written into it, ready to stage.
     * The matrix test needs one per deployment: distinct LargeObjects, same artifact content.
     */
    private LargeObject createElmLargeObjectWithContent() throws IOException {
        return createElmLargeObjectWithContent(MONGO);
    }

    /**
     * A fresh ELM LargeObject with the supplied test element artifact's ELM content written into it, ready
     * to stage.
     */
    private LargeObject createElmLargeObjectWithContent(final TestElementArtifact artifact) throws IOException {
        final var elm = createElmLargeObject();
        writeElmContent(elm.getId(), artifact);
        return elm;
    }

    private ElementDeployment newDeployment(final LargeObject elm) {
        return newDeployment(elm, "hot-reload-under-test", null);
    }

    /**
     * Builds a deployment whose {@code name} and {@code application} may each be {@code null}: the
     * four-deployment matrix test covers one deployment with only a name, one with only an application,
     * one with both, and one with neither.
     */
    private ElementDeployment newDeployment(final LargeObject elm, final String name, final Application application) {        return new ElementDeployment(
                null,                                           // id -- assigned
                name,                                           // name -- optional, falls back to the id
                application,                                    // application -- null deploys system wide
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
        writeElmContent(largeObjectId, MONGO);
    }

    private void writeElmContent(final String largeObjectId, final TestElementArtifact artifact) throws IOException {
        try (final var os = largeObjectBucket.writeObject(largeObjectId);
             final var is = ElementArtifactLoader.newDefaultInstance()
                     .findArtifact(ArtifactRepository.DEFAULTS, artifact.getCoordinatesForElm())
                     .map(Artifact::read)
                     .orElseThrow(() -> new IllegalStateException(
                             artifact + " test element ELM not found in local repository: "
                                     + artifact.getCoordinatesForElm()))) {
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
