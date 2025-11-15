package dev.getelements.elements.sdk.mongo.standard;

import com.mongodb.ConnectionString;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.mongo.MongoConfiguration;
import dev.getelements.elements.sdk.mongo.MongoConfigurationService;
import dev.getelements.elements.sdk.mongo.MongoSslConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;

public class StandardMongoConfigurationService implements MongoConfigurationService {

    private static final Logger logger = LoggerFactory.getLogger(StandardMongoConfigurationService.class);

    private String mongoDbUri;

    private String sslProtocol;

    private String keyAlgorithm;

    private String trustAlgorithm;

    private String keyFormat;

    private String caPath;

    private String caPassphrase;

    private String clientCertificatePath;

    private String clientCertificatePassphrase;

    @Override
    public MongoConfiguration getMongoConfiguration() {
        return new MongoConfiguration(
                getMongoDbUri(),
                getMongoSslConfiguration()
        );
    }

    private MongoSslConfiguration getMongoSslConfiguration() {

        final var connectString = new ConnectionString(getMongoDbUri());
        final var sslEnabled = connectString.getSslEnabled();

        if (sslEnabled != null && sslEnabled) {
            logger.info("MongoDB SSL is enabled for URI: {}", getMongoDbUri());
        } else {
            logger.info("MongoDB SSL is disabled for URI: {}", getMongoDbUri());
            return null;
        }

        try {

            logger.info("Enabling TLS/SSL.");

            final var ca = KeyStore.getInstance(getKeyFormat());
            final var certificate = KeyStore.getInstance(getKeyFormat());

            try (var fis = new FileInputStream(getCaPath())) {
                final var passphrase = getCaPassphrase();
                ca.load(fis, passphrase.isEmpty() ? null : passphrase.toCharArray());
            }

            try (var fis = new FileInputStream(getClientCertificatePath())) {
                final var passphrase = getClientCertificatePassphrase();
                certificate.load(fis, passphrase.isEmpty() ? null : passphrase.toCharArray());
            }

            final var tmf = TrustManagerFactory.getInstance(getTrustAlgorithm());
            tmf.init(ca);

            final var kmf = KeyManagerFactory.getInstance(getKeyAlgorithm());
            kmf.init(certificate, getClientCertificatePassphrase().toCharArray());

            final var sslInvalidHostnameAllowed = connectString.getSslInvalidHostnameAllowed();

            logger.info("Enabled TLS/SSL.");
            return new MongoSslConfiguration(
                    kmf.getKeyManagers(),
                    tmf.getTrustManagers(),
                    getSslProtocol(),
                    sslInvalidHostnameAllowed != null && sslInvalidHostnameAllowed
            );

        } catch (IOException |
                 NoSuchAlgorithmException |
                 CertificateException |
                 KeyStoreException |
                 UnrecoverableKeyException ex) {
            logger.warn("Caught exception loading TLS/SSL Keys.", ex);
            return null;
        }

    }

    public String getMongoDbUri() {
        return mongoDbUri;
    }

    @Inject
    public void setMongoDbUri(@Named(MONGO_CLIENT_URI) String mongoDbUri) {
        this.mongoDbUri = mongoDbUri;
    }

    public String getSslProtocol() {
        return sslProtocol;
    }

    @Inject
    public void setSslProtocol(@Named(SSL_PROTOCOL) String sslProtocol) {
        this.sslProtocol = sslProtocol;
    }

    public String getKeyAlgorithm() {
        return keyAlgorithm;
    }

    @Inject
    public void setKeyAlgorithm(@Named(KEY_ALGORITHM) String keyAlgorithm) {
        this.keyAlgorithm = keyAlgorithm;
    }

    public String getKeyFormat() {
        return keyFormat;
    }

    @Inject
    public void setKeyFormat(@Named(FORMAT) String keyFormat) {
        this.keyFormat = keyFormat;
    }

    public String getCaPath() {
        return caPath;
    }

    @Inject
    public void setCaPath(@Named(CA) String caPath) {
        this.caPath = caPath;
    }

    public String getCaPassphrase() {
        return caPassphrase;
    }

    @Inject
    public void setCaPassphrase(@Named(CA_PASSPHRASE) String caPassphrase) {
        this.caPassphrase = caPassphrase;
    }

    public String getClientCertificatePath() {
        return clientCertificatePath;
    }

    @Inject
    public void setClientCertificatePath(@Named(CLIENT_CERTIFICATE) String clientCertificatePath) {
        this.clientCertificatePath = clientCertificatePath;
    }

    public String getClientCertificatePassphrase() {
        return clientCertificatePassphrase;
    }

    @Inject
    public void setClientCertificatePassphrase(@Named(CLIENT_CERTIFICATE_PASSPHRASE) String clientCertificatePassphrase) {
        this.clientCertificatePassphrase = clientCertificatePassphrase;
    }

    public String getTrustAlgorithm() {
        return trustAlgorithm;
    }

    @Inject
    public void setTrustAlgorithm(@Named(TRUST_ALGORITHM) String trustAlgorithm) {
        this.trustAlgorithm = trustAlgorithm;
    }

}
