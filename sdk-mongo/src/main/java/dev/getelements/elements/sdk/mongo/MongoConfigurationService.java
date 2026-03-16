package dev.getelements.elements.sdk.mongo;

import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.util.function.Function;

/**
 * Interface which reads the Namazu Elements system configuration and provides configuration required to connect to
 * MongoDB. This enables clients to connect directly to MongoDB.
 */
@ElementServiceExport
public interface MongoConfigurationService {

    @ElementDefaultAttribute(value = "mongodb://localhost", description = "The MongoDB Client URI.")
    String MONGO_CLIENT_URI = "dev.getelements.elements.mongo.uri";

    @ElementDefaultAttribute(value = "PKCS12", description = "The format of the TLS/SSL keys.")
    String FORMAT = "dev.getelements.elements.mongo.tls.format";

    @ElementDefaultAttribute(
            supplier = DefaultTrustManagerAlgorithm.class,
            description = "Specifies the TrustManagerFactory algorithm to use. (Defaults to System Default)"
    )
    String TRUST_ALGORITHM =  "dev.getelements.elements.mongo.tls.trust.algorithm";

    @ElementDefaultAttribute(
            supplier = DefaultKeyManagerAlgorithm.class,
            description = "Specifies the KeyManagerFactory algorithm to use. (Defaults to System Default)"
    )
    String KEY_ALGORITHM =  "dev.getelements.elements.mongo.tls.key.algorithm";

    @ElementDefaultAttribute(
            description = "The path to the Certificate Authority (CA) file. (May be Blank)."
    )
    String CA = "dev.getelements.elements.mongo.tls.ca";

    @ElementDefaultAttribute(
            sensitive = true,
            description = "The passphrase for the Certificate Authority (CA) file. (May be Blank)."
    )
    String CA_PASSPHRASE = "dev.getelements.elements.mongo.tls.ca.passphrase";

    @ElementDefaultAttribute(
            sensitive = true,
            description = "The path to the client certificate file."
    )
    String CLIENT_CERTIFICATE = "dev.getelements.elements.mongo.tls.client.certificate";

    @ElementDefaultAttribute(
            sensitive = true,
            description = "The passphrase for the client certificate file."
    )
    String CLIENT_CERTIFICATE_PASSPHRASE = "dev.getelements.elements.mongo.tls.client.certificate.passphrase";

    @ElementDefaultAttribute(value = "TLS", description = "The TLS/SSL protocol to use.")
    String SSL_PROTOCOL = "dev.getelements.elements.mongo.tls.protocol";

    @ElementDefaultAttribute(
            value = "elements",
            description = "The name of the MongoDB database to use for Elements data storage."
    )
    String DATABASE_NAME = "dev.getelements.elements.mongo.database.name";

    /**
     * Retrieves the MongoDB configuration for the Namazu Elements system.
     *
     * @return the {@link MongoConfiguration} containing connection details
     */
    MongoConfiguration getMongoConfiguration();

    /**
     * Returns the default {@link KeyManagerFactory} algorithm. This defers to the
     * {@link KeyManagerFactory#getDefaultAlgorithm()} result.
     */
    class DefaultKeyManagerAlgorithm implements Function<ElementDefaultAttribute, String> {
        @Override
        public String apply(final ElementDefaultAttribute attribute) {
            return KeyManagerFactory.getDefaultAlgorithm();
        }
    }

    /**
     * Returns the default {@link TrustManagerFactory} algorithm. This defers to the
     * {@link TrustManagerFactory#getDefaultAlgorithm()} result.
     */
    class DefaultTrustManagerAlgorithm implements Function<ElementDefaultAttribute, String> {
        @Override
        public String apply(ElementDefaultAttribute attribute) {
            return TrustManagerFactory.getDefaultAlgorithm();
        }
    }

}
