package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.PSNApplicationConfiguration;
import org.testng.annotations.Guice;

import static org.testng.Assert.assertEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoPSNApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<PSNApplicationConfiguration> {

    @Override
    protected Class<PSNApplicationConfiguration> getTestType() {
        return PSNApplicationConfiguration.class;
    }

    @Override
    protected PSNApplicationConfiguration createTestObject() {
        final PSNApplicationConfiguration config = new PSNApplicationConfiguration();
        config.setNpIdentifier("com.example.myapplication");
        config.setClientSecret("clientSecret");
        return config;
    }

    @Override
    protected PSNApplicationConfiguration updateTestObject(PSNApplicationConfiguration config) {
        config.setNpIdentifier("com.example.myapplication-update");
        config.setClientSecret("clientSecret-update");
        return config;
    }

    @Override
    protected void assertCreatedCorrectly(PSNApplicationConfiguration actual, PSNApplicationConfiguration expected) {
        assertEquals(actual.getNpIdentifier(), expected.getNpIdentifier());
        assertEquals(actual.getClientSecret(), expected.getClientSecret());
    }

}
