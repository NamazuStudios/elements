package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationConfigurationNotFoundException;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.IntSupplier;
import java.util.stream.Stream;

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

    protected abstract Class<UnderTestT> getTestType();

    protected abstract UnderTestT createTestObject();

    protected abstract UnderTestT updateTestObject(UnderTestT config);

    protected abstract void assertCreatedCorrectly(UnderTestT actual, UnderTestT expected);

    private final Map<String, UnderTestT> intermediates = new ConcurrentHashMap<>();

    public Stream<String> streamScopes() {
        return Stream.of(application.getId(), application.getName());
    }

    @DataProvider
    public Object[][] getScope() {
        return streamScopes()
                .map(i -> new Object[] {i})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getScopeAndIntermediates() {
        return intermediates
                .values()
                .stream()
                .flatMap(i -> streamScopes().map(scope -> new Object[] {scope, i}))
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getDeleteCandidates() {

        final var counter = new AtomicInteger();
        final IntSupplier flagSupplier = () -> counter.getAndIncrement() % 4;

        return intermediates
                .values()
                .stream()
                .map(intermediate -> {

                    final var flag = flagSupplier.getAsInt();

                    return new Object[] {
                            (flag & 0x01) == 0 ? application.getId() : application.getName(),
                            (flag & 0x02) == 0 ? intermediate.getId() : intermediate.getName()
                    };

                })
                .toArray(Object[][]::new);

    }

    @BeforeClass
    public void setupApplication() {
        application = applicationTestFactory.createAtomicApplication("Test Configuration.");
        application = applicationDao.createOrUpdateInactiveApplication(application);
    }

    @Test(
        invocationCount = 50,
        threadPoolSize = 10,
        groups = "createApplicationConfiguration",
        dataProvider = "getScope"
    )
    public void testCreate(final String scope) {

        final var config = createTestObject();
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var created = applicationConfigurationDao.createApplicationConfiguration(scope, config);
        assertNotNull(created.getId(), "Created configuration id is null");
        assertEquals(created.getName(), config.getName(), "Name fields are not equal");
        assertEquals(created.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(created.getType(), config.getType(), "Type fields are not equal");
        assertEquals(created.getParent(), config.getParent(), "Parent fields are not equal");
        assertCreatedCorrectly(created, config);

        intermediates.put(created.getId(), created);

    }

    @Test(
        dataProvider = "getScopeAndIntermediates",
        dependsOnGroups = "createApplicationConfiguration",
        expectedExceptions = DuplicateException.class
    )
    public void testCreateDuplicateFails(final String scope, final UnderTestT intermediate) {
        final var toFail = createTestObject();
        toFail.setName(intermediate.getName());
        toFail.setDescription(intermediate.getDescription());
        toFail.setType(intermediate.getType());
        toFail.setParent(intermediate.getParent());
        applicationConfigurationDao.createApplicationConfiguration(scope, toFail);
    }

    @Test(
        threadPoolSize = 10,
        dataProvider = "getScopeAndIntermediates",
        groups = "updateApplicationConfiguration",
        dependsOnGroups = "createApplicationConfiguration"
    )
    public void testUpdate(final String scope, final UnderTestT intermediate) {

        final var config = updateTestObject(intermediate);
        final var sequence = counter.incrementAndGet();
        final var className = config.getClass().getName();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", className));
        config.setType(className);

        final var updated = applicationConfigurationDao.updateApplicationConfiguration(scope, config);
        assertNotNull(updated.getId(), "Created configuration id is null");
        assertEquals(updated.getName(), config.getName(), "Name fields are not equal");
        assertEquals(updated.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(updated.getType(), config.getType(), "Type fields are not equal");
        assertEquals(updated.getParent(), config.getParent(), "Parent fields are not equal");
        intermediates.put(intermediate.getId(), updated);

    }

    @Test(
        dataProvider = "getScope",
        groups = "fetchApplicationConfiguration",
        dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetAllConfigurationsForType(final String scope) {
        final var all = applicationConfigurationDao.getAllActiveApplicationConfigurations(scope, getTestType());
        all.forEach(c -> assertTrue(intermediates.containsKey(c.getId())));
    }

    @Test(
            dataProvider = "getScopeAndIntermediates",
            groups = "fetchApplicationConfiguration",
            dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetSpecificApplicationConfigurationById(final String scope, final UnderTestT intermediate) {

        final var fetched = applicationConfigurationDao.getApplicationConfiguration(
                getTestType(),
                scope,
                intermediate.getId()
        );

        assertEquals(fetched, intermediate);

    }

    @Test(
            dataProvider = "getScopeAndIntermediates",
            groups = "fetchApplicationConfiguration",
            dependsOnGroups = "updateApplicationConfiguration"
    )
    public void testGetSpecificApplicationConfigurationByName(final String scope, final UnderTestT intermediate) {

        final var fetched = applicationConfigurationDao.getApplicationConfiguration(
                getTestType(),
                scope,
                intermediate.getName()
        );

        assertEquals(fetched, intermediate);

    }

    @Test(
            dataProvider = "getDeleteCandidates",
            groups = "deleteApplicationConfiguration",
            dependsOnGroups = "fetchApplicationConfiguration"
    )
    public void testDelete(final String applicationScope,
                           final String applicationConfigurationScope) {
        applicationConfigurationDao.deleteApplicationConfiguration(
                getTestType(),
                applicationScope,
                applicationConfigurationScope
        );
    }

    @Test(
            dataProvider = "getDeleteCandidates",
            groups = "deleteApplicationConfiguration",
            dependsOnGroups = "fetchApplicationConfiguration",
            dependsOnMethods = "testDelete",
            expectedExceptions = ApplicationConfigurationNotFoundException.class
    )
    public void testDoubleDelete(final String applicationScope,
                                 final String applicationConfigurationScope) {
        applicationConfigurationDao.deleteApplicationConfiguration(
                getTestType(),
                applicationScope,
                applicationConfigurationScope
        );
    }

}
