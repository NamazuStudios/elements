package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.CallbackDefinition;
import dev.getelements.elements.sdk.model.application.ElementServiceReference;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import org.testng.annotations.Guice;

import static java.lang.String.format;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoMatchmakingApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<MatchmakingApplicationConfiguration> {

    @Override
    protected Class<MatchmakingApplicationConfiguration> getTestType() {
        return MatchmakingApplicationConfiguration.class;
    }

    @Override
    protected MatchmakingApplicationConfiguration createTestObject() {

        final var service = new ElementServiceReference();
        service.setElementName("dev.getelements.test.element.a");
        service.setServiceType("dev.getelements.test.MatchMaker");
        service.setServiceName("some-service");

        final var success = new CallbackDefinition();
        success.setMethod("success");
        success.setService(service);

        final var config = new MatchmakingApplicationConfiguration();
        config.setSuccess(success);

        return config;

    }

    @Override
    protected MatchmakingApplicationConfiguration updateTestObject(final MatchmakingApplicationConfiguration config) {

        final var service = new ElementServiceReference();
        service.setElementName("dev.getelements.test.element.b");
        service.setServiceType("dev.getelements.test.MatchMaker");

        final var success = new CallbackDefinition();
        success.setMethod("success");
        success.setService(service);

        final var matchmaker = new ElementServiceReference();
        matchmaker.setElementName("dev.getelements.test.element.c");

        config.setSuccess(success);
        config.setMatchmaker(matchmaker);
        config.setName(format("%s_updated", config.getName()));

        return config;

    }

    @Override
    protected void assertCreatedCorrectly(
            final MatchmakingApplicationConfiguration actual,
            final MatchmakingApplicationConfiguration expected) {
        assertNotNull(actual.getSuccess());
        assertEquals(actual.getSuccess(), expected.getSuccess());
        assertEquals(actual.getMatchmaker(), expected.getMatchmaker());
    }

}
