package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.exception.system.ElementDeploymentNotFoundException;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoElementDeploymentDaoTest {

    private ElementDeploymentDao elementDeploymentDao;

    private ApplicationTestFactory applicationTestFactory;

    private final List<ElementDeployment> deployments = new CopyOnWriteArrayList<>();

    @DataProvider
    public Object[][] allDeployments() {
        return deployments.stream()
                .map(d -> new Object[]{d})
                .toArray(Object[][]::new);
    }

    @Test(groups = "createElementDeployment")
    public void testCreateDeploymentWithApplication() {
        final var application = getApplicationTestFactory().createMockApplication(getClass());
        final var deployment = new ElementDeployment(
                null,
                application,
                List.of("com.example:api:1.0"),
                List.of("com.example:spi:1.0"),
                List.of("com.example:element:1.0"),
                null,
                null,
                true,
                List.of(new ArtifactRepository("central", "https://repo.maven.apache.org/maven2")),
                ElementDeploymentState.ENABLED
        );

        final var created = getElementDeploymentDao().createElementDeployment(deployment);
        assertNotNull(created.id());
        assertNotNull(created.application());
        assertEquals(created.application().getId(), application.getId());
        assertEquals(created.apiArtifacts(), deployment.apiArtifacts());
        assertEquals(created.spiArtifacts(), deployment.spiArtifacts());
        assertEquals(created.elementArtifacts(), deployment.elementArtifacts());
        assertNull(created.elm());
        assertNull(created.elmArtifact());
        assertTrue(created.useDefaultRepositories());
        assertEquals(created.repositories().size(), 1);
        assertEquals(created.state(), ElementDeploymentState.ENABLED);

        deployments.add(created);
    }

    @Test(groups = "createElementDeployment")
    public void testCreateDeploymentWithoutApplication() {
        final var deployment = new ElementDeployment(
                null,
                null,
                List.of("com.example:api-global:1.0"),
                List.of("com.example:spi-global:1.0"),
                List.of(),
                null,
                "com.example:elm:1.0",
                false,
                List.of(),
                ElementDeploymentState.UNLOADED
        );

        final var created = getElementDeploymentDao().createElementDeployment(deployment);
        assertNotNull(created.id());
        assertNull(created.application());
        assertEquals(created.apiArtifacts(), deployment.apiArtifacts());
        assertEquals(created.spiArtifacts(), deployment.spiArtifacts());
        assertEquals(created.elmArtifact(), "com.example:elm:1.0");
        assertFalse(created.useDefaultRepositories());
        assertEquals(created.state(), ElementDeploymentState.UNLOADED);

        deployments.add(created);
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment",
            dataProvider = "allDeployments"
    )
    public void testGetById(final ElementDeployment deployment) {
        final var fetched = getElementDeploymentDao().getElementDeployment(deployment.id());
        assertNotNull(fetched);
        assertEquals(fetched.id(), deployment.id());
        assertEquals(fetched.state(), deployment.state());
        assertEquals(fetched.apiArtifacts(), deployment.apiArtifacts());
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment",
            dataProvider = "allDeployments"
    )
    public void testFindById(final ElementDeployment deployment) {
        final var found = getElementDeploymentDao().findElementDeployment(deployment.id());
        assertTrue(found.isPresent());
        assertEquals(found.get().id(), deployment.id());
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment"
    )
    public void testFindByInvalidId() {
        final var found = getElementDeploymentDao().findElementDeployment("not_a_valid_id");
        assertTrue(found.isEmpty());
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment"
    )
    public void testGetAllWithPagination() {
        final var page = getElementDeploymentDao().getElementDeployments(0, 10, null);
        assertNotNull(page);
        assertTrue(page.getTotal() >= 2);
        assertNotNull(page.getObjects());
        assertFalse(page.getObjects().isEmpty());
    }

    @Test(
            groups = "updateElementDeployment",
            dependsOnGroups = "fetchElementDeployment",
            dataProvider = "allDeployments"
    )
    public void testUpdateStateAndArtifacts(final ElementDeployment deployment) {
        final var updated = new ElementDeployment(
                deployment.id(),
                deployment.application(),
                List.of("com.example:api-updated:2.0"),
                List.of("com.example:spi-updated:2.0"),
                deployment.elementArtifacts(),
                deployment.elm(),
                deployment.elmArtifact(),
                deployment.useDefaultRepositories(),
                deployment.repositories(),
                ElementDeploymentState.DISABLED
        );

        final var result = getElementDeploymentDao().updateElementDeployment(updated);
        assertEquals(result.id(), deployment.id());
        assertEquals(result.apiArtifacts(), List.of("com.example:api-updated:2.0"));
        assertEquals(result.spiArtifacts(), List.of("com.example:spi-updated:2.0"));
        assertEquals(result.state(), ElementDeploymentState.DISABLED);

        deployments.remove(deployment);
        deployments.add(result);
    }

    @Test(
            groups = "deleteElementDeployment",
            dependsOnGroups = "updateElementDeployment",
            dataProvider = "allDeployments"
    )
    public void testDeleteById(final ElementDeployment deployment) {
        getElementDeploymentDao().deleteDeployment(deployment.id());
    }

    @Test(
            groups = "deleteElementDeployment",
            dependsOnMethods = "testDeleteById",
            expectedExceptions = ElementDeploymentNotFoundException.class,
            dataProvider = "allDeployments"
    )
    public void testDoubleDelete(final ElementDeployment deployment) {
        getElementDeploymentDao().deleteDeployment(deployment.id());
    }

    @Test(
            groups = "deleteElementDeployment",
            dependsOnMethods = "testDeleteById",
            expectedExceptions = ElementDeploymentNotFoundException.class,
            dataProvider = "allDeployments"
    )
    public void testFetchAfterDelete(final ElementDeployment deployment) {
        getElementDeploymentDao().getElementDeployment(deployment.id());
    }

    public ElementDeploymentDao getElementDeploymentDao() {
        return elementDeploymentDao;
    }

    @Inject
    public void setElementDeploymentDao(ElementDeploymentDao elementDeploymentDao) {
        this.elementDeploymentDao = elementDeploymentDao;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

}
