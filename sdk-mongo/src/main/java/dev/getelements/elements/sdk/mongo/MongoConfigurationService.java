package dev.getelements.elements.sdk.mongo;

/**
 * Interface which reads the Namazu Elements system configuration and provides configuration required to connect to
 * MongoDB. This enables clients to connect directly to MongoDB.
 */
public interface MongoConfigurationService {

    /**
     * Retrieves the MongoDB configuration for the Namazu Elements system.
     *
     * @return the {@link MongoConfiguration} containing connection details
     */
    MongoConfiguration getMongoConfiguration();

}
