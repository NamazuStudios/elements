package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.config.ModuleDefaults;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.Properties;

import static dev.getelements.elements.dao.mongo.MongoConcurrentUtils.*;
import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
import static dev.getelements.elements.dao.mongo.provider.MongoDatastoreProvider.DATABASE_NAME;
import static dev.getelements.elements.dao.mongo.provider.MongoSslSettingsProvider.*;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDaoModuleDefaults implements ModuleDefaults {

    public static final int OPTISMITIC_RETRY_COUNT = 10;

    public static final int DEFAULT_FALLOFF_TIME_MIN_MS = 50;

    public static final int DEFAULT_FALLOFF_TIME_MAX_MS = 150;

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        defaultProperties.setProperty(DATABASE_NAME, "elements");
        defaultProperties.setProperty(MONGO_CLIENT_URI, "mongodb://localhost");
        defaultProperties.setProperty(FORMAT, "PKCS12");
        defaultProperties.setProperty(SSL_PROTOCOL, "TLS");
        defaultProperties.setProperty(KEY_ALGORITHM, KeyManagerFactory.getDefaultAlgorithm());
        defaultProperties.setProperty(TRUST_ALGORITHM, TrustManagerFactory.getDefaultAlgorithm());
        defaultProperties.setProperty(CA, "");
        defaultProperties.setProperty(CA_PASSPHRASE, "");
        defaultProperties.setProperty(CLIENT_CERTIFICATE, "");
        defaultProperties.setProperty(CLIENT_CERTIFICATE_PASSPHRASE, "");
        defaultProperties.setProperty(FALLOFF_TIME_MIN_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MIN_MS));
        defaultProperties.setProperty(FALLOFF_TIME_MAX_MS, Integer.toString(DEFAULT_FALLOFF_TIME_MAX_MS));
        defaultProperties.setProperty(OPTIMISTIC_RETRY_COUNT, Integer.toString(OPTISMITIC_RETRY_COUNT));
        return defaultProperties;
    }

}
