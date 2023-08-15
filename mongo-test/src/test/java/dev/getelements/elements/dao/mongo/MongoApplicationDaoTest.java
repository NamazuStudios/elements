package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import org.testng.annotations.BeforeTest;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoApplicationDaoTest {

    private static final String TEST_APPLICATION_DESC = "Test Application.";
    private static final String TEST_APPLICATION_0_NAME = "test_application_0";

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private ApplicationDao applicationDao;

    @BeforeTest
    public void setup() {
        applicationDao = applicationTestFactory.getApplicationDao();
        applicationTestFactory
                .createMockApplicationWithSingleAttribute(TEST_APPLICATION_DESC, "k1", "val1");
        applicationTestFactory
                .createMockApplicationWithSingleAttribute(TEST_APPLICATION_DESC, "k2", "val2");
    }

    @Test
    public void testCreateSimpleApplication() {
        final Application application = applicationTestFactory.createAtomicApplication(TEST_APPLICATION_DESC);

        Application createdApplication = applicationDao.createOrUpdateInactiveApplication(application);
        Application foundApplication = applicationDao.getActiveApplication(createdApplication.getName());

        assertNotNull(foundApplication);
        assertEquals(application.getDescription(), foundApplication.getDescription());
    }

    @Test
    public void testCreateApplicationWithAttributes() {
        Application application = applicationTestFactory.createAtomicApplication(TEST_APPLICATION_DESC);
        application.setAttributes(applicationTestFactory.createMockAttributes(
                asList("k1", "k2", "k3"), asList("val1", "val2", "val3")));

        Application created = applicationDao.createOrUpdateInactiveApplication(application);
        Map<String, Object> newAppAttributes = applicationDao.getActiveApplication(created.getName()).getAttributes();

        assertEquals(application.getAttributes().size(), newAppAttributes.size());
        newAppAttributes.keySet().forEach(key ->
            assertEquals(newAppAttributes.get(key), application.getAttributes().get(key)
        ));
    }

    @Test
    public void testUpdateActiveApplication() {
        Application application = applicationTestFactory.createAtomicApplication("updatedDescription");
        application.setAttributes(applicationTestFactory.createSingleAttributesMock("updatedKey", "updatedValue"));

        Application toUpdate = applicationDao.getActiveApplications().getObjects().get(0);
        applicationDao.updateActiveApplication(toUpdate.getName(), application);
        Application afterUpdate = applicationDao.getActiveApplications().getObjects().get(0);

        assertEquals(afterUpdate.getDescription(), "updatedDescription");
        assertEquals(afterUpdate.getAttributes().size(), 1);
        assertEquals(afterUpdate.getAttributes().get("updatedKey"), "updatedValue");
    }

    @Test(expectedExceptions = ApplicationNotFoundException.class)
    public void testSoftDeleteApplication() {
        Application toDelete = applicationDao.getActiveApplication(TEST_APPLICATION_0_NAME);

        Pagination<Application> beforeDelete = applicationDao.getActiveApplications();
        applicationDao.softDeleteApplication(toDelete.getId());
        Pagination<Application> afterDelete = applicationDao.getActiveApplications();

        assertEquals(beforeDelete.getTotal() - 1, afterDelete.getTotal());
        applicationDao.getActiveApplication(TEST_APPLICATION_0_NAME);
    }

    @Test
    public void testGetActiveApplications() {
        Pagination<Application> foundApplications = applicationDao.getActiveApplications();

        assertNotNull(foundApplications);
        assertEquals(foundApplications.getObjects().get(0).getDescription(), TEST_APPLICATION_DESC);
        assertEquals(foundApplications.getObjects().get(0).getAttributes().get("k1"), "val1");
        assertEquals(foundApplications.getObjects().get(1).getAttributes().get("k2"), "val2");
    }

    @Test
    public void testGetActiveApplicationsRange() {
        Pagination<Application> foundApplications = applicationDao.getActiveApplications(0,1);
        Application firstFoundApplication = foundApplications.getObjects().get(0);

        assertEquals(foundApplications.getObjects().size(), 1);
        assertEquals(firstFoundApplication.getDescription(), TEST_APPLICATION_DESC);
        assertEquals(firstFoundApplication.getAttributes().get("k1"), "val1");
    }

    @Test
    public void testGetActiveApplicationsByName() {
        Application foundApplication = applicationDao.getActiveApplication(TEST_APPLICATION_0_NAME);

        assertEquals(foundApplication.getDescription(), TEST_APPLICATION_DESC);
        assertEquals(foundApplication.getAttributes().get("k1"), "val1");
    }

    @Test
    public void testGetAppliactionWithoutAttributes() {
        Application foundApplication = applicationDao.getActiveApplicationWithoutAttributes(TEST_APPLICATION_0_NAME);

        assertEquals(foundApplication.getDescription(), TEST_APPLICATION_DESC);
        assertTrue(foundApplication.getAttributes().isEmpty());
    }
}
