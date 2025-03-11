package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.Application;
import dev.morphia.Datastore;
import dev.morphia.annotations.Entity;
import dev.morphia.annotations.Id;
import dev.morphia.annotations.Property;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.HashMap;
import java.util.Map;

import static dev.morphia.query.filters.Filters.eq;

@Guice(modules = IntegrationTestModule.class)
public class MongoApplicationConfigurationDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoApplicationConfigurationDaoTest.class);

    @Inject
    private Datastore ds;

    @Inject
    private ApplicationTestFactory applicationTestFactory;

    private Application application;

    @BeforeClass
    public void setupApplication() {
        application = applicationTestFactory.createAtomicApplication("Test Configuration.");
    }

    @Test
    public void setupApplicationConfiguration() {
        final var tm = new TestModel();
        tm.objectMap = new HashMap<>();
        tm.objectMap.put("base", new TestEmbedBase());
        tm.objectMap.put("derived", new TestEmbedDerived());

        ds.save(tm);

        final var tmResult = ds
                .find(TestModel.class)
                .filter(eq("_id", tm.objectId))
                .stream()
                .findFirst();

        logger.info("Success: {}", tmResult);

    }

    @Entity
    private static class TestModel {

        @Id
        public ObjectId objectId;

        @Property
        public Map<String, Object> objectMap;

    }

    @Entity
    private static class TestEmbedBase {
        public String foo = "foo";
    }

    @Entity
    public static class TestEmbedDerived {
        private String bar = "bar";
    }

}
