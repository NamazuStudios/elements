package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import org.testng.annotations.Guice;

import java.util.List;

import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoFacebookApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<FacebookApplicationConfiguration> {


    @Override
    protected Class<FacebookApplicationConfiguration> getTestType() {
        return FacebookApplicationConfiguration.class;
    }

    @Override
    protected FacebookApplicationConfiguration createTestObject() {
        final var config = new FacebookApplicationConfiguration();
        config.setApplicationId("com.example.myapplication");
        config.setApplicationSecret("example-secret");
        return config;
    }

    @Override
    protected FacebookApplicationConfiguration updateTestObject(final FacebookApplicationConfiguration config) {
        config.setApplicationId("com.example.myapplication.update");
        config.setApplicationSecret("example-secret-update");
        return config;
    }

    @Override
    protected void assertCreatedCorrectly(
            final FacebookApplicationConfiguration actual,
            final FacebookApplicationConfiguration expected) {
        assertNull(actual.getBuiltinApplicationPermissions());
        assertEquals(actual.getApplicationId(), expected.getApplicationId());
        assertEquals(actual.getApplicationSecret(), expected.getApplicationSecret());
    }


}
