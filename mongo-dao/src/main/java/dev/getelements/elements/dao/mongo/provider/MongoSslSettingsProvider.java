package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.ConnectionString;
import com.mongodb.connection.SslSettings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;
import java.io.FileInputStream;
import java.io.IOException;
import java.security.*;
import java.security.cert.CertificateException;

import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;

public class MongoSslSettingsProvider implements Provider<SslSettings> {

    public static final String FORMAT =  "dev.getelements.elements.mongo.tls.format";

    public static final String TRUST_ALGORITHM =  "dev.getelements.elements.mongo.tls.trust.algorithm";

    public static final String KEY_ALGORITHM =  "dev.getelements.elements.mongo.tls.key.algorithm";

    public static final String CA = "dev.getelements.elements.mongo.tls.ca";

    public static final String CA_PASSPHRASE = "dev.getelements.elements.mongo.tls.ca.passphrase";

    public static final String CLIENT_CERTIFICATE = "dev.getelements.elements.mongo.tls.client.certificate";

    public static final String CLIENT_CERTIFICATE_PASSPHRASE = "dev.getelements.elements.mongo.tls.client.certificate.passphrase";

    public static final String SSL_PROTOCOL = "dev.getelements.elements.mongo.tls.protocol";

    private static final Logger logger = LoggerFactory.getLogger(MongoSslSettingsProvider.class);

    private String sslProtocol;

    private String keyAlgorithm;

    private String trustAlgorithm;

    private String keyFormat;

    private String caPath;

    private String caPassphrase;

    private String clientCertificatePath;

    private String clientCertificatePassphrase;

    private String clientUri;

    @Override
    public SslSettings get() {

        final var connectString = new ConnectionString(getClientUri());
        final var sslEnabled = connectString.getSslEnabled();

        if (sslEnabled == null || !sslEnabled) {
            logger.info("TLS/SSL Is not Enabled. Please explicitly enable it in the connect string.");
            return SslSettings.builder().enabled(false).build();
        }

        try {

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

            final var sslContext = SSLContext.getInstance(getSslProtocol());
            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());

            var settings = SslSettings.builder()
                    .enabled(true)
                    .context(sslContext)
                    .applyConnectionString(connectString);

            logger.info("Enabled TLS/SSL.");
            return settings.build();

        } catch (IOException |
                 NoSuchAlgorithmException |
                 CertificateException |
                 KeyStoreException |
                 UnrecoverableKeyException |
                 KeyManagementException ex) {
            logger.warn("Caught exception loading TLS/SSL Keys.", ex);
            return SslSettings.builder().enabled(false).build();
        }

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

    public String getClientUri() {
        return clientUri;
    }

    @Inject
    public void setClientUri(@Named(MONGO_CLIENT_URI) String clientUri) {
        this.clientUri = clientUri;
    }

}
