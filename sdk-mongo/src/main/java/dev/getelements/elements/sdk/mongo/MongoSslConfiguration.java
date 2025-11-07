package dev.getelements.elements.sdk.mongo;

import dev.getelements.elements.sdk.exception.SdkException;

import javax.net.ssl.KeyManager;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;

/**
 * The MongoDB SSL configuration record. Contains SSL Specific configuration for MongoDB connections.
 * @param keyManager one or more KeyManagers to use for SSL connections these contain the client key material
 * @param trustManager one or more TrustManagers to use for SSL connections these contain the trusted CA certificates
 */
public record MongoSslConfiguration(KeyManager[] keyManager, TrustManager[] trustManager, String sslProtocol) {

    /**
     * Creates a new {@link SSLContext} from the SSL configuration.
     * @return the new SSLContext
     */
    public SSLContext newSslContext() {
        return newSslContext(new SecureRandom());
    }

    /**
     * Creates a new {@link SSLContext} from the SSL configuration.
     * @param secureRandom the SecureRandom to use
     * @return the new SSLContext
     */
    private SSLContext newSslContext(final SecureRandom secureRandom) {
        try {
            final var context =  SSLContext.getInstance(sslProtocol());
            context.init(keyManager(), trustManager(), secureRandom);
            return context;
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            throw new SdkException(e);
        }
    }

}
