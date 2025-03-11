package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.sdk.model.application.Application;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoApplicationDaoTest {

    private static final String TEST_APPLICATION_DESC = "Test Application.";
    private static final String TEST_APPLICATION_0_NAME = "test_application_0";

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private ApplicationDao applicationDao;

    private List<Application> applications = new CopyOnWriteArrayList<>();

    @Test(groups = "create_application")
    public void testCreateSimpleApplication() {
        final Application application = applicationTestFactory.createAtomicApplication(TEST_APPLICATION_DESC);

        Application createdApplication = getApplicationDao().createOrUpdateInactiveApplication(application);
        Application foundApplication = getApplicationDao().getActiveApplication(createdApplication.getName());

        assertNotNull(foundApplication);
        assertEquals(application.getDescription(), foundApplication.getDescription());
        applications.add(createdApplication);
    }

    @Test(groups = "create_application")
    public void testCreateApplicationWithAttributes() {

        Application application = applicationTestFactory.createAtomicApplication(TEST_APPLICATION_DESC);
        application.setAttributes(applicationTestFactory.createMockAttributes(
                asList("k1", "k2", "k3"), asList("val1", "val2", "val3")));

        Application created = getApplicationDao().createOrUpdateInactiveApplication(application);
        Map<String, Object> newAppAttributes = getApplicationDao().getActiveApplication(created.getName()).getAttributes();

        assertEquals(application.getAttributes().size(), newAppAttributes.size());
        newAppAttributes.keySet().forEach(key ->
            assertEquals(newAppAttributes.get(key), application.getAttributes().get(key)
        ));

        applications.add(created);

    }

    @DataProvider
    public Object[][] allApplications() {
        return applications.stream()
                .map(a -> new Object[]{a})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "allApplications", groups = "fetch_application", dependsOnGroups = "create_application")
    public void testGetActiveApplicationsById(final Application application) {
        Application foundApplication = getApplicationDao().getActiveApplication(application.getId());
        assertEquals(foundApplication.getDescription(), application.getDescription());
        assertEquals(foundApplication.getAttributes(), application.getAttributes());
    }

    @Test(dataProvider = "allApplications", groups = "fetch_application", dependsOnGroups = "create_application")
    public void testGetActiveApplicationsByName(final Application application) {
        Application foundApplication = getApplicationDao().getActiveApplication(application.getName());
        assertEquals(foundApplication.getDescription(), application.getDescription());
        assertEquals(foundApplication.getAttributes(), application.getAttributes());
    }

    @Test(dataProvider = "allApplications", groups = "fetch_application", dependsOnGroups = "create_application")
    public void testGetActiveApplications(final Application application) {

        final var foundApplications = getApplicationDao().getActiveApplications();
        assertNotNull(foundApplications);

        final var applicationFromResults = foundApplications
                .stream()
                .filter(a -> application.getId().equals(a.getId()))
                .findFirst()
                .get();

        assertEquals(applicationFromResults.getId(), application.getId());
        assertEquals(applicationFromResults.getName(), application.getName());
        assertEquals(applicationFromResults.getDescription(), application.getDescription());
        assertEquals(applicationFromResults.getAttributes(), application.getAttributes());

    }

    @Test(dataProvider = "allApplications", groups = "update_application", dependsOnGroups = "fetch_application")
    public void testUpdateActiveApplication(final Application application) {

        final var updatedDescription = "Updated - " + TEST_APPLICATION_DESC;

        final var toUpdate = new Application();
        toUpdate.setId(application.getId());
        toUpdate.setName(application.getName());
        toUpdate.setDescription(updatedDescription);
        toUpdate.setAttributes(application.getAttributes());

        getApplicationDao().updateActiveApplication(application.getName(), toUpdate);

        Application afterUpdate;

        afterUpdate = getApplicationDao().getActiveApplication(application.getId());
        assertEquals(afterUpdate.getId(), application.getId());
        assertEquals(afterUpdate.getName(), application.getName());
        assertEquals(afterUpdate.getDescription(), updatedDescription);
        assertEquals(afterUpdate.getAttributes(), application.getAttributes());

        afterUpdate = getApplicationDao().getActiveApplication(application.getName());
        assertEquals(afterUpdate.getId(), application.getId());
        assertEquals(afterUpdate.getName(), application.getName());
        assertEquals(afterUpdate.getDescription(), updatedDescription);
        assertEquals(afterUpdate.getAttributes(), application.getAttributes());

        applications.remove(application);
        applications.add(afterUpdate);

    }

    @Test(dataProvider = "allApplications", groups = "delete_application", dependsOnGroups = "update_application")
    public void testSoftDeleteApplication(final Application application) {
        getApplicationDao().softDeleteApplication(application.getId());
    }

    @Test(dataProvider = "allApplications",
            groups = "delete_application",
            dependsOnGroups = "update_application",
            dependsOnMethods = "testSoftDeleteApplication",
            expectedExceptions = ApplicationNotFoundException.class)
    public void testDoubleDeleteApplication(final Application application) {
        getApplicationDao().softDeleteApplication(application.getId());
    }

    @Test(dataProvider = "allApplications",
            groups = "delete_application",
            dependsOnGroups = "update_application",
            dependsOnMethods = "testSoftDeleteApplication",
            expectedExceptions = ApplicationNotFoundException.class)
    public void testFetchPostDeleteApplicationById(final Application application) {
        getApplicationDao().getActiveApplication(application.getId());
    }

    @Test(dataProvider = "allApplications",
            groups = "delete_application",
            dependsOnGroups = "update_application",
            dependsOnMethods = "testSoftDeleteApplication",
            expectedExceptions = ApplicationNotFoundException.class)
    public void testFetchPostDeleteApplicationByName(final Application application) {
        getApplicationDao().getActiveApplication(application.getName());
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

    public ApplicationDao getApplicationDao() {
        return applicationDao;
    }

    @Inject
    public void setApplicationDao(ApplicationDao applicationDao) {
        this.applicationDao = applicationDao;
    }

}
