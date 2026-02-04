package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.exception.system.ElementDeploymentNotFoundException;
import dev.getelements.elements.sdk.model.system.ElementArtifactRepository;
import dev.getelements.elements.sdk.model.system.ElementDefinition;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.ElementDeploymentDao.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoElementDeploymentDaoTest {

    private ElementDeploymentDao elementDeploymentDao;

    private ApplicationTestFactory applicationTestFactory;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final List<ElementDeployment> deployments = new CopyOnWriteArrayList<>();

    private final List<ElementDeployment> createdDeployments = new CopyOnWriteArrayList<>();

    private final List<ElementDeployment> updatedDeployments = new CopyOnWriteArrayList<>();

    private final List<ElementDeployment> deletedDeployments = new CopyOnWriteArrayList<>();

    @DataProvider
    public Object[][] allDeployments() {
        return deployments.stream()
                .map(d -> new Object[]{d})
                .toArray(Object[][]::new);
    }

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case ELEMENT_DEPLOYMENT_CREATED -> onCreate(ev.getEventArgument(0));
                case ELEMENT_DEPLOYMENT_UPDATED -> onUpdate(ev.getEventArgument(0));
                case ELEMENT_DEPLOYMENT_DELETED -> onDelete(ev.getEventArgument(0));
            }
        });
    }

    private void onCreate(final ElementDeployment deployment) {
        createdDeployments.add(deployment);
    }

    private void onUpdate(final ElementDeployment deployment) {
        updatedDeployments.add(deployment);
    }

    private void onDelete(final ElementDeployment deployment) {
        deletedDeployments.add(deployment);
    }

    @Test(groups = "createElementDeployment")
    public void testCreateDeploymentWithApplication() {
        final var application = getApplicationTestFactory().createMockApplication(getClass());

        final var elementDefinition = new ElementDefinition(
                List.of("com.example:api:1.0"),
                List.of("com.example:spi:1.0"),
                List.of("com.example:element:1.0"),
                null
        );

        final var deployment = new ElementDeployment(
                null,
                application,
                null,
                List.of(elementDefinition),
                true,
                List.of(new ElementArtifactRepository("central", "https://repo.maven.apache.org/maven2")),
                null,
                ElementDeploymentState.ENABLED,
                0L
        );

        final var created = getElementDeploymentDao().createElementDeployment(deployment);
        assertNotNull(created.id());
        assertNotNull(created.application());
        assertEquals(created.application().getId(), application.getId());
        assertNotNull(created.elements());
        assertEquals(created.elements().size(), 1);
        assertEquals(created.elements().get(0).apiArtifacts(), elementDefinition.apiArtifacts());
        assertEquals(created.elements().get(0).spiArtifacts(), elementDefinition.spiArtifacts());
        assertEquals(created.elements().get(0).elementArtifacts(), elementDefinition.elementArtifacts());
        assertNull(created.elm());
        assertTrue(created.useDefaultRepositories());
        assertEquals(created.repositories().size(), 1);
        assertEquals(created.state(), ElementDeploymentState.ENABLED);

        // Verify event was fired
        assertTrue(createdDeployments.stream().anyMatch(d -> d.id().equals(created.id())),
                "ELEMENT_DEPLOYMENT_CREATED event should have been fired");

        deployments.add(created);
    }

    @Test(groups = "createElementDeployment")
    public void testCreateDeploymentWithoutApplication() {
        final var elementDefinition = new ElementDefinition(
                List.of("com.example:api-global:1.0"),
                List.of("com.example:spi-global:1.0"),
                null,
                "com.example:elm:1.0"
        );

        final var deployment = new ElementDeployment(
                null,
                null,
                null,
                List.of(elementDefinition),
                false,
                List.of(),
                null,
                ElementDeploymentState.UNLOADED,
                0L
        );

        final var created = getElementDeploymentDao().createElementDeployment(deployment);
        assertNotNull(created.id());
        assertNull(created.application());
        assertNotNull(created.elements());
        assertEquals(created.elements().size(), 1);
        assertEquals(created.elements().get(0).apiArtifacts(), elementDefinition.apiArtifacts());
        assertEquals(created.elements().get(0).spiArtifacts(), elementDefinition.spiArtifacts());
        assertEquals(created.elements().get(0).elmArtifact(), "com.example:elm:1.0");
        assertFalse(created.useDefaultRepositories());
        assertEquals(created.state(), ElementDeploymentState.UNLOADED);

        // Verify event was fired
        assertTrue(createdDeployments.stream().anyMatch(d -> d.id().equals(created.id())),
                "ELEMENT_DEPLOYMENT_CREATED event should have been fired");

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
        assertEquals(fetched.elements(), deployment.elements());
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
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment"
    )
    public void testGetByStateEnabled() {
        final var enabledDeployments = getElementDeploymentDao().getElementDeploymentsByState(ElementDeploymentState.ENABLED);
        assertNotNull(enabledDeployments);
        assertFalse(enabledDeployments.isEmpty());
        for (final var deployment : enabledDeployments) {
            assertEquals(deployment.state(), ElementDeploymentState.ENABLED);
        }
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment"
    )
    public void testGetByStateUnloaded() {
        final var unloadedDeployments = getElementDeploymentDao().getElementDeploymentsByState(ElementDeploymentState.UNLOADED);
        assertNotNull(unloadedDeployments);
        assertFalse(unloadedDeployments.isEmpty());
        for (final var deployment : unloadedDeployments) {
            assertEquals(deployment.state(), ElementDeploymentState.UNLOADED);
        }
    }

    @Test(
            groups = "fetchElementDeployment",
            dependsOnGroups = "createElementDeployment"
    )
    public void testGetByStateDisabled() {
        // No DISABLED deployments created yet, should return empty list
        final var disabledDeployments = getElementDeploymentDao().getElementDeploymentsByState(ElementDeploymentState.DISABLED);
        assertNotNull(disabledDeployments);
        // May be empty since we haven't created any DISABLED deployments yet
    }

    @Test(
            groups = "updateElementDeployment",
            dependsOnGroups = "fetchElementDeployment",
            dataProvider = "allDeployments"
    )
    public void testUpdateStateAndArtifacts(final ElementDeployment deployment) {
        final var updatedDefinition = new ElementDefinition(
                List.of("com.example:api-updated:2.0"),
                List.of("com.example:spi-updated:2.0"),
                deployment.elements() != null && !deployment.elements().isEmpty()
                        ? deployment.elements().get(0).elementArtifacts()
                        : List.of(),
                deployment.elements() != null && !deployment.elements().isEmpty()
                        ? deployment.elements().get(0).elmArtifact()
                        : null
        );

        final var updated = new ElementDeployment(
                deployment.id(),
                deployment.application(),
                deployment.elm(),
                List.of(updatedDefinition),
                deployment.useDefaultRepositories(),
                deployment.repositories(),
                deployment.attributes(),
                ElementDeploymentState.DISABLED,
                deployment.version()
        );

        final var result = getElementDeploymentDao().updateElementDeployment(updated);
        assertEquals(result.id(), deployment.id());
        assertNotNull(result.elements());
        assertEquals(result.elements().size(), 1);
        assertEquals(result.elements().get(0).apiArtifacts(), List.of("com.example:api-updated:2.0"));
        assertEquals(result.elements().get(0).spiArtifacts(), List.of("com.example:spi-updated:2.0"));
        assertEquals(result.state(), ElementDeploymentState.DISABLED);
        assertEquals(result.version(), deployment.version() + 1, "Version should be incremented on update");

        // Verify event was fired
        assertTrue(updatedDeployments.stream().anyMatch(d -> d.id().equals(result.id())),
                "ELEMENT_DEPLOYMENT_UPDATED event should have been fired");

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

        // Verify event was fired
        assertTrue(deletedDeployments.stream().anyMatch(d -> d.id().equals(deployment.id())),
                "ELEMENT_DEPLOYMENT_DELETED event should have been fired");
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
