package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoSteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import org.mapstruct.factory.Mappers;
import org.testng.annotations.Test;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the sandbox flag round-trips through the Steam application configuration mapping. The flag
 * was added with the Steam verification fix (#223-era) but was missing from the persistence model, so
 * it could not be stored: the admin UI's Sandbox checkbox appeared to save while the value was
 * silently dropped, and verification always queried the live ISteamMicroTxn endpoint.
 */
public class MongoSteamApplicationConfigurationMapperSandboxTest {

    @Test
    public void sandboxRoundTripsFromMongoToSdkModel() {

        final var mongo = new MongoSteamApplicationConfiguration();
        mongo.setSandbox(true);

        final var mapper = Mappers.getMapper(MongoSteamApplicationConfigurationMapper.class);
        final var sdkModel = mapper.forward(mongo);

        assertTrue(sdkModel.getSandbox(), "sandbox must map from the persistence model to the SDK model");

    }

    @Test
    public void sandboxRoundTripsFromSdkToMongoModel() {

        final var sdkModel = new SteamApplicationConfiguration();
        sdkModel.setSandbox(true);

        final var mapper = Mappers.getMapper(MongoSteamApplicationConfigurationMapper.class);
        final var mongo = mapper.reverse(sdkModel);

        assertTrue(mongo.getSandbox(), "sandbox must map from the SDK model to the persistence model");

    }

    @Test
    public void unsetSandboxMapsAsNull() {

        final var mongo = new MongoSteamApplicationConfiguration();

        final var mapper = Mappers.getMapper(MongoSteamApplicationConfigurationMapper.class);
        final var sdkModel = mapper.forward(mongo);

        assertNull(sdkModel.getSandbox(), "an unset sandbox must map as null (live endpoint)");

    }

}
