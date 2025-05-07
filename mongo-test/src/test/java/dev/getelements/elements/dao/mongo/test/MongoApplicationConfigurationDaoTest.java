package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.util.PaginationWalker;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

import static java.lang.String.format;
import static org.testng.Assert.*;

public abstract class MongoApplicationConfigurationDaoTest<UnderTestT extends ApplicationConfiguration> {

    private static final Logger logger = LoggerFactory.getLogger(MongoApplicationConfigurationDaoTest.class);

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    @Inject
    private ApplicationDao applicationDao;

    @Inject
    private ApplicationConfigurationDao applicationConfigurationDao;

    private Application application;

    private final AtomicInteger counter = new AtomicInteger();

    protected abstract Class<? extends ApplicationConfiguration> getTestType();

    protected abstract UnderTestT createTestObject();

    protected abstract UnderTestT updateTestObject(UnderTestT config);

    protected abstract void assertCreatedCorrectly(UnderTestT actual, UnderTestT expected);

    private final Map<String, UnderTestT> intermediates = new ConcurrentHashMap<>();

    @DataProvider
    public Object[][] getIntermediates() {
        return intermediates
                .values()
                .stream()
                .map(i -> new Object[] {i})
                .toArray(Object[][]::new);

    }

    @BeforeClass
    public void setupApplication() {
        application = applicationTestFactory.createAtomicApplication("Test Configuration.");
        application = applicationDao.createOrUpdateInactiveApplication(application);
    }

    @Test(invocationCount = 100, threadPoolSize = 10, groups = "createApplicationConfiguration")
    public void testCreateWithApplicationId() {

        final var config = createTestObject();
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var created = applicationConfigurationDao.createApplicationConfiguration(
                application.getId(),
                config
        );

        assertNotNull(created.getId(), "Created configuration id is null");
        assertEquals(created.getName(), config.getName(), "Name fields are not equal");
        assertEquals(created.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(created.getType(), config.getType(), "Type fields are not equal");
        assertEquals(created.getParent(), config.getParent(), "Parent fields are not equal");
        assertCreatedCorrectly(created, config);

        intermediates.put(created.getId(), created);

    }

    @Test(
        invocationCount = 100,
        threadPoolSize = 10,
        groups = "createApplicationConfiguration"
    )
    public void testCreateWithApplicationName() {

        final var config = createTestObject();
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var created = applicationConfigurationDao.createApplicationConfiguration(
                application.getName(),
                config
        );

        assertNotNull(created.getId(), "Created configuration id is null");
        assertEquals(created.getName(), config.getName(), "Name fields are not equal");
        assertEquals(created.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(created.getType(), config.getType(), "Type fields are not equal");
        assertEquals(created.getParent(), config.getParent(), "Parent fields are not equal");
        assertCreatedCorrectly(created, config);

        intermediates.put(created.getId(), created);

    }

    @Test(
        dataProvider = "getIntermediates",
        dependsOnGroups = "createApplicationConfiguration",
        expectedExceptions = DuplicateException.class
    )
    public void testCreateDuplicateFailsById(UnderTestT intermediate) {

        final var toFail = createTestObject();
        toFail.setName(intermediate.getName());
        toFail.setDescription(intermediate.getDescription());
        toFail.setType(intermediate.getType());
        toFail.setParent(intermediate.getParent());

        applicationConfigurationDao.createApplicationConfiguration(
                application.getId(),
                toFail
        );

    }

    @Test(
        dataProvider = "getIntermediates",
        dependsOnGroups = "createApplicationConfiguration",
        expectedExceptions = DuplicateException.class
    )
    public void testCreateDuplicateFailsByName(UnderTestT intermediate) {

        final var toFail = createTestObject();
        toFail.setName(intermediate.getName());
        toFail.setDescription(intermediate.getDescription());
        toFail.setType(intermediate.getType());
        toFail.setParent(intermediate.getParent());

        applicationConfigurationDao.createApplicationConfiguration(
                application.getName(),
                toFail
        );

    }

    @Test(
        threadPoolSize = 10,
        dataProvider = "getIntermediates",
        groups = "updateApplicationConfiguration",
        dependsOnGroups = "createApplicationConfiguration"
    )
    public void testUpdateWithApplicationId(UnderTestT intermediate) {

        final var config = updateTestObject(intermediate);
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var updated = applicationConfigurationDao.updateApplicationConfiguration(
                application.getId(),
                config
        );

        assertNotNull(updated.getId(), "Created configuration id is null");
        assertEquals(updated.getName(), config.getName(), "Name fields are not equal");
        assertEquals(updated.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(updated.getType(), config.getType(), "Type fields are not equal");
        assertEquals(updated.getParent(), config.getParent(), "Parent fields are not equal");

    }

    @Test(
        threadPoolSize = 10,
        dataProvider = "getIntermediates",
        groups = "updateApplicationConfiguration",
        dependsOnGroups = "createApplicationConfiguration"
    )
    public void testUpdateWithApplicationName(UnderTestT intermediate) {

        final var config = updateTestObject(intermediate);
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var updated = applicationConfigurationDao.updateApplicationConfiguration(
                application.getName(),
                config
        );

        assertNotNull(updated.getId(), "Created configuration id is null");
        assertEquals(updated.getName(), config.getName(), "Name fields are not equal");
        assertEquals(updated.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(updated.getType(), config.getType(), "Type fields are not equal");
        assertEquals(updated.getParent(), config.getParent(), "Parent fields are not equal");

    }

    @Test(
        groups = "fetchApplicationConfiguration",
        dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetAllConfigurationsForTypeById() {

        final var all = applicationConfigurationDao.getAllActiveApplicationConfigurations(
                application.getId(),
                getTestType()
        );

        all.forEach(c -> assertTrue(intermediates.containsKey(c.getId())));

    }

    @Test(
        groups = "fetchApplicationConfiguration",
        dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetAllConfigurationsForTypeByName() {

        final var all = applicationConfigurationDao.getAllActiveApplicationConfigurations(
                application.getName(),
                getTestType()
        );

        all.forEach(c -> assertTrue(intermediates.containsKey(c.getId())));

    }

    @Test(
        groups = "fetchApplicationConfiguration",
        dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetConfigurationsForTypeById() {

        final PaginationWalker.WalkFunction<ApplicationConfiguration> walkFunction =
                (offset, count) -> applicationConfigurationDao.getActiveApplicationConfigurations(
                        application.getId(),
                        offset,
                        count
                );

        final var configurations = new PaginationWalker()
                .toList(walkFunction)
                .stream()
                .filter(c -> getTestType().getName().equals(c.getType()))
                .toList();

        assertEquals(configurations.size(), intermediates.size());
        configurations.forEach(c -> assertTrue(intermediates.containsKey(c.getId())));

    }

    @Test(
        groups = "fetchApplicationConfiguration",
        dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetConfigurationsForTypeByName() {

        final PaginationWalker.WalkFunction<ApplicationConfiguration> walkFunction =
                (offset, count) -> applicationConfigurationDao.getActiveApplicationConfigurations(
                    application.getName(),
                    offset,
                    count
            );

        final var configurations = new PaginationWalker()
                .toList(walkFunction)
                .stream()
                .filter(c -> getTestType().getName().equals(c.getType()))
                .toList();

        assertEquals(configurations.size(), intermediates.size());
        configurations.forEach(c -> assertTrue(intermediates.containsKey(c.getId())));

    }

}
