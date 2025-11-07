package dev.getelements.elements.sdk.mongo;

import java.util.Optional;

/**
 * The MongoDB configuration record. This contains all necessary information to access the MongoDB instance configured
 * for Namazu Elements
 *
 * @param connectionString the database connection string
 * @param sslConfiguration the SSL configuration, may be null if SSL is not used
 */
public record MongoConfiguration(
        String connectionString,
        MongoSslConfiguration sslConfiguration
) {

    public MongoConfiguration {
        if (connectionString == null || connectionString.isBlank()) {
            throw new IllegalArgumentException("Connection string must not be null or blank");
        }
    }

    /**
     * Finds the SSL configuration for the MongoDB connection, if any. May be empty if SSL is not configured and the
     * connection is not using SSL.
     *
     * @return the {@link MongoSslConfiguration} if SSL is configured, empty otherwise
     */
    public Optional<MongoSslConfiguration> findSessionConfiguration() {
        return Optional.ofNullable(sslConfiguration);
    }

}
