//package dev.getelements.elements.dao.mongo.provider;
//
//import com.mongodb.ConnectionString;
//import com.mongodb.connection.SslSettings;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import jakarta.inject.Inject;
//import jakarta.inject.Named;
//import jakarta.inject.Provider;
//import javax.net.ssl.KeyManagerFactory;
//import javax.net.ssl.SSLContext;
//import javax.net.ssl.TrustManagerFactory;
//import java.io.FileInputStream;
//import java.io.IOException;
//import java.security.*;
//import java.security.cert.CertificateException;
//
//import static dev.getelements.elements.dao.mongo.provider.MongoClientProvider.MONGO_CLIENT_URI;
//
//public class MongoSslSettingsProvider implements Provider<SslSettings> {
//
//    private static final Logger logger = LoggerFactory.getLogger(MongoSslSettingsProvider.class);
//
//    private String sslProtocol;
//
//    private String keyAlgorithm;
//
//    private String trustAlgorithm;
//
//    private String keyFormat;
//
//    private String caPath;
//
//    private String caPassphrase;
//
//    private String clientCertificatePath;
//
//    private String clientCertificatePassphrase;
//
//    private String clientUri;
//
//    @Override
//    public SslSettings get() {
//
//        final var connectString = new ConnectionString(getClientUri());
//        final var sslEnabled = connectString.getSslEnabled();
//
//        if (sslEnabled == null || !sslEnabled) {
//            logger.info("TLS/SSL Is not Enabled. Please explicitly enable it in the connect string.");
//            return SslSettings.builder().enabled(false).build();
//        }
//
//        try {
//
//            final var ca = KeyStore.getInstance(getKeyFormat());
//            final var certificate = KeyStore.getInstance(getKeyFormat());
//
//            try (var fis = new FileInputStream(getCaPath())) {
//                final var passphrase = getCaPassphrase();
//                ca.load(fis, passphrase.isEmpty() ? null : passphrase.toCharArray());
//            }
//
//            try (var fis = new FileInputStream(getClientCertificatePath())) {
//                final var passphrase = getClientCertificatePassphrase();
//                certificate.load(fis, passphrase.isEmpty() ? null : passphrase.toCharArray());
//            }
//
//            final var tmf = TrustManagerFactory.getInstance(getTrustAlgorithm());
//            tmf.init(ca);
//
//            final var kmf = KeyManagerFactory.getInstance(getKeyAlgorithm());
//            kmf.init(certificate, getClientCertificatePassphrase().toCharArray());
//
//            final var sslContext = SSLContext.getInstance(getSslProtocol());
//            sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), new SecureRandom());
//
//            var settings = SslSettings.builder()
//                    .enabled(true)
//                    .context(sslContext)
//                    .applyConnectionString(connectString);
//
//            logger.info("Enabled TLS/SSL.");
//            return settings.build();
//
//        } catch (IOException |
//                 NoSuchAlgorithmException |
//                 CertificateException |
//                 KeyStoreException |
//                 UnrecoverableKeyException |
//                 KeyManagementException ex) {
//            logger.warn("Caught exception loading TLS/SSL Keys.", ex);
//            return SslSettings.builder().enabled(false).build();
//        }
//
//    }
//
//
//    public String getClientUri() {
//        return clientUri;
//    }
//
//    @Inject
//    public void setClientUri(@Named(MONGO_CLIENT_URI) String clientUri) {
//        this.clientUri = clientUri;
//    }
//
//}
