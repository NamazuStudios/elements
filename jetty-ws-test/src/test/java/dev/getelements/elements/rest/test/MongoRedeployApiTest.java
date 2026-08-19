package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.dao.EntityRegistry;
import dev.getelements.elements.sdk.deployment.ElementRuntimeService;
import dev.getelements.elements.sdk.deployment.TransientDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import org.testng.Assert;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static dev.getelements.elements.sdk.test.TestElementArtifact.API;
import static dev.getelements.elements.sdk.test.TestElementArtifact.MONGO;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;

/**
 * Reproduces issue #40 end-to-end: {@link #datastore} is injected exactly once, at test-fixture
 * construction time, mirroring the real-world eager-singleton {@code MongoMeteringBatchDao} that
 * captured a raw {@code Datastore} and held onto it for its whole lifetime. The Element deployed by
 * this test (see {@code sdk-test-element-mongo}) declares a Morphia entity purely so that a genuine
 * unload/reload gives two distinct {@code Class} objects for the same entity name
 * ("test_redeploy_document") -- exactly what a real Element redeploy produces.
 *
 * <p>Between the two deploys, the long-held {@link #datastore} saves a real instance of whichever
 * generation's entity {@code Class} is currently loaded. Morphia's {@code Mapper} caches entity
 * models by class <em>name</em> (see {@code Mapper.register}/{@code getEntityModel}), not by
 * {@code Class} identity, and silently keeps the first-registered model on a name collision. Pre-fix,
 * the field holds a single, now-abandoned {@code Datastore}/{@code Mapper} snapshot captured before
 * either deploy: the first {@code save()} registers the first generation's {@code Class} under
 * "test_redeploy_document", and the second {@code save()} -- a genuinely different {@code Class}
 * object with the same name -- gets handed back the *first* generation's {@code EntityModel} and
 * tries to read/write its fields via {@code java.lang.reflect.Field}s obtained from the first
 * generation's {@code Class}, against an instance of the second -- the exact
 * {@code IllegalArgumentException} ("Can not set ... field ... to ...", i.e. two {@code Class}
 * objects with the same name) reported in issue #40. Post-fix, the field holds a
 * {@code LiveDatastore} proxy that transparently resolves to the current {@code Datastore} on every
 * call, so it never sees a stale {@code Mapper}.
 */
public class MongoRedeployApiTest {

    @Factory
    public Object[] getTests() {
        return new Object[] {
                TestUtils.getInstance().getTestFixture(MongoRedeployApiTest.class)
        };
    }

    @Inject
    private Datastore datastore;

    @Inject
    private ElementRuntimeService runtimeService;

    private TransientDeploymentRequest buildDeployment() {
        return TransientDeploymentRequest.builder()
                .useDefaultRepositories(true)
                .addElement(new ElementPathDefinition(
                        "mongo",
                        API.getAllCoordinates().toList(),
                        List.of("DEFAULT"),
                        GUICE_7_0_X.getAllCoordinates().toList(),
                        MONGO.getAllCoordinates().toList(),
                        Map.of()
                ))
                .build();
    }

    private Class<?> entityClassOf(final ElementRuntimeService.RuntimeRecord runtimeRecord) {
        final var element = runtimeRecord.elements().getFirst();
        final var entityRegistry = element.getServiceLocator().getInstance(EntityRegistry.class);
        return entityRegistry.entityClasses().getFirst();
    }

    @Test
    public void survivesARedeploy() throws ReflectiveOperationException {

        final var first = runtimeService.loadTransientDeployment(buildDeployment());
        final var firstGenerationEntityClass = entityClassOf(first);

        datastore.save(newDocument(firstGenerationEntityClass, "doc-before-redeploy"));

        final var unloaded = runtimeService.unloadTransientDeployment(first.deployment().id());
        Assert.assertTrue(unloaded, "Expected the first deployment to be found and unloaded");

        final var second = runtimeService.loadTransientDeployment(buildDeployment());
        final var secondGenerationEntityClass = entityClassOf(second);

        Assert.assertNotSame(
                firstGenerationEntityClass,
                secondGenerationEntityClass,
                "A genuine redeploy must produce a distinct Class object for the same entity name -- "
                        + "otherwise this test isn't reproducing issue #40 at all"
        );

        // On main (pre-fix), `datastore` is a single Datastore/Mapper snapshot captured before either
        // deploy ever ran. Saving the first generation's instance registered "test_redeploy_document"
        // -> firstGenerationEntityClass in that Mapper; saving the second generation's instance below
        // reuses the SAME cached EntityModel (Morphia keys by class name, not identity) and tries to
        // read/write its fields via Fields obtained from the first generation's Class against an
        // instance of the second -- Morphia's "two Class objects, same name" IllegalArgumentException.
        // On the fix branch, `datastore` is a LiveDatastore proxy that always resolves to the current
        // Datastore/Mapper, so this succeeds.
        datastore.save(newDocument(secondGenerationEntityClass, "doc-after-redeploy"));

        runtimeService.unloadTransientDeployment(second.deployment().id());

    }

    private static Object newDocument(final Class<?> entityClass, final String id) throws ReflectiveOperationException {
        final var instance = entityClass.getDeclaredConstructor().newInstance();
        entityClass.getMethod("setId", String.class).invoke(instance, id);
        entityClass.getMethod("setText", String.class).invoke(instance, "text-for-" + id);
        return instance;
    }

}
