package dev.getelements.elements.service;

import javax.ws.rs.client.*;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.security.PublicKey;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.security.interfaces.RSAPublicKey;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

public class GoogleSignInCertificateHelper {

    private static final Map<String, String> googleSignInCerts = new HashMap<>();

    public static Map<String, String> getGoogleSignInCerts() throws ExecutionException, InterruptedException {
        if(googleSignInCerts.isEmpty()) {
            fetchGoogleSignInCerts();
        }

        return googleSignInCerts;
    }

    public static String certForKeyId(String keyId) throws ExecutionException, InterruptedException {
        if(googleSignInCerts.isEmpty() || !googleSignInCerts.containsKey(keyId)) {
            //Attempt to refresh just in case Google changed things up
            fetchGoogleSignInCerts();

            if(!googleSignInCerts.containsKey(keyId)) {
                throw new ExecutionException(new Throwable("Google certificate does not exist for the given key id."));
            }
        }

        return googleSignInCerts.get(keyId);
    }

    public static PublicKey certToPublicKey(final String cert) throws CertificateException, IOException {

        final var certificateFactory = CertificateFactory.getInstance("X.509");
        final var inputStream = new ByteArrayInputStream(cert.getBytes());
        final var x509Certificate = (X509Certificate) certificateFactory.generateCertificate(inputStream);
        final var publicKey = (RSAPublicKey) x509Certificate.getPublicKey();
        inputStream.close();

        return publicKey;
    }

    private static void fetchGoogleSignInCerts() throws ExecutionException, InterruptedException {

        final var url = "https://www.googleapis.com/oauth2/v1/certs";
        final var client = ClientBuilder.newClient();
        final var response = client.target(url)
                .request()
                .header("Content-Type", "application/json")
                .get()
                .readEntity(HashMap.class);


        googleSignInCerts.putAll(response);
    }
}
