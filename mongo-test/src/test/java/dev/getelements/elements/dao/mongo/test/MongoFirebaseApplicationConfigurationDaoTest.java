package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import org.testng.annotations.Guice;

import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoFirebaseApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<FirebaseApplicationConfiguration> {

    @Override
    protected Class<FirebaseApplicationConfiguration> getTestType() {
        return FirebaseApplicationConfiguration.class;
    }

    @Override
    protected FirebaseApplicationConfiguration createTestObject() {
        final FirebaseApplicationConfiguration config = new FirebaseApplicationConfiguration();
        config.setProjectId("com.example.myapplication");
        config.setServiceAccountCredentials("example-secret");
        return config;
    }

    @Override
    protected FirebaseApplicationConfiguration updateTestObject(FirebaseApplicationConfiguration config) {
        config.setProjectId("com.example.myapplication.update");
        config.setServiceAccountCredentials("example-secret-update");
        return config;
    }

    @Override
    protected void assertCreatedCorrectly(FirebaseApplicationConfiguration actual, FirebaseApplicationConfiguration expected) {
        assertEquals(actual.getProjectId(), expected.getProjectId());
        assertEquals(actual.getServiceAccountCredentials(), expected.getServiceAccountCredentials());
    }

}
