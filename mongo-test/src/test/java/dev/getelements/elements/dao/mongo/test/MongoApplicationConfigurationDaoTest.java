package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ApplicationConfigurationDao;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.morphia.Datastore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

import static dev.morphia.query.filters.Filters.eq;
import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

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

    protected abstract UnderTestT create();

    protected abstract UnderTestT update(UnderTestT config);

    protected abstract void checkCreated(UnderTestT expected, UnderTestT actual);

    private final List<UnderTestT> intermediate = new CopyOnWriteArrayList<>();

    @BeforeClass
    public void setupApplication() {
        application = applicationTestFactory.createAtomicApplication("Test Configuration.");
        application = applicationDao.createOrUpdateInactiveApplication(application);
    }

    @Test(invocationCount = 100, threadPoolSize = 10)
    public void testCreate() {

        final var config = create();
        final var sequence = counter.incrementAndGet();
        final var simpleName = config.getClass().getSimpleName();

        config.setParent(application);
        config.setName(format("%s_%d", simpleName, sequence));
        config.setDescription(format("Test configuration for %s", simpleName));
        config.setType(simpleName);

        final var created = applicationConfigurationDao.createApplicationConfiguration(
                application.getId(),
                config
        );

        assertNotNull(created.getId(), "Created configuration id is null");
        assertEquals(created.getName(), config.getName(), "Name fields are not equal");
        assertEquals(created.getDescription(), config.getDescription(), "Description fields are not equal");
        assertEquals(created.getType(), config.getType(), "Type fields are not equal");
        assertEquals(created.getParent(), config.getParent(), "Parent fields are not equal");

        intermediate.add(created);

    }

}
