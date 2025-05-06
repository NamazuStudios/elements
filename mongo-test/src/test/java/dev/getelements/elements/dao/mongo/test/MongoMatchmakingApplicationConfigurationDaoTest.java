package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.application.CallbackDefinition;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import org.testng.annotations.Guice;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoMatchmakingApplicationConfigurationDaoTest extends MongoApplicationConfigurationDaoTest<MatchmakingApplicationConfiguration> {

    @Override
    protected MatchmakingApplicationConfiguration create() {

        final var success = new CallbackDefinition();
        success.setMethod("success");
        success.setModule("def.getelements.test.MatchMaker");

        final var config = new MatchmakingApplicationConfiguration();
        config.setSuccess(success);

        return config;

    }

    @Override
    protected MatchmakingApplicationConfiguration update(final MatchmakingApplicationConfiguration config) {
        final var success = new CallbackDefinition();
        success.setMethod("success");
        success.setModule("dev.getelements.test.Updated");
        config.setSuccess(success);
        return config;
    }

    @Override
    protected void checkCreated(
            final MatchmakingApplicationConfiguration actual,
            final MatchmakingApplicationConfiguration expected) {
        assertNotNull(actual.getSuccess());
        assertEquals(actual.getSuccess(), expected.getSuccess());
    }

}
