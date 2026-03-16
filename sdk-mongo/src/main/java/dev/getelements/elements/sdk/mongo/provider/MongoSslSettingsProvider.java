package dev.getelements.elements.sdk.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.connection.SslSettings;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static dev.getelements.elements.sdk.mongo.MongoConfigurationService.MONGO_CLIENT_URI;

public class MongoSslSettingsProvider implements Provider<SslSettings> {

    private static final Logger logger = LoggerFactory.getLogger(MongoSslSettingsProvider.class);

    private String clientUri;

    private MongoConfigurationService mongoConfigurationService;

    @Override
    public SslSettings get() {

        final var connectString = new ConnectionString(getClientUri());
        final var sslEnabled = connectString.getSslEnabled();

        if (sslEnabled == null || !sslEnabled) {
            logger.info("TLS/SSL Is not Enabled. Please explicitly enable it in the connect string.");
            return SslSettings.builder().enabled(false).build();
        }

        final var configuration = getMongoConfigurationService().getMongoConfiguration();
        final var sslContext = configuration.sslConfiguration().newSslContext();

        var settings = SslSettings.builder()
                .enabled(true)
                .context(sslContext)
                .applyConnectionString(connectString);

        logger.info("Enabled TLS/SSL.");
        return settings.build();

    }

    public MongoConfigurationService getMongoConfigurationService() {
        return mongoConfigurationService;
    }

    @Inject
    public void setMongoConfigurationService(MongoConfigurationService mongoConfigurationService) {
        this.mongoConfigurationService = mongoConfigurationService;
    }

    public String getClientUri() {
        return clientUri;
    }

    @Inject
    public void setClientUri(@Named(MONGO_CLIENT_URI) String clientUri) {
        this.clientUri = clientUri;
    }

}
