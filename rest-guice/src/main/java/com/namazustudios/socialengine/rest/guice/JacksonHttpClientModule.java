package com.namazustudios.socialengine.rest.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;

import javax.inject.Provider;
import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.ext.ContextResolver;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;

import static javax.ws.rs.client.ClientBuilder.newBuilder;

public class JacksonHttpClientModule extends PrivateModule {

    public static final String DISABLE_SSL_VERIFICATION = "com.namazustudios.ssl.verification.disable";

    static {

        final String disable = System.getProperty(DISABLE_SSL_VERIFICATION);

        if (disable != null && Boolean.valueOf(disable)) {
            disableSslVerification();
        }

    }

    private static void disableSslVerification() {
        try
        {
            // Create a trust manager that does not validate certificate chains
            TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                public void checkClientTrusted(X509Certificate[] certs, String authType) {
                }
                public void checkServerTrusted(X509Certificate[] certs, String authType) {
                }
            }
            };

            // Install the all-trusting trust manager
            SSLContext sc = SSLContext.getInstance("SSL");
            sc.init(null, trustAllCerts, new java.security.SecureRandom());
            HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());

            // Create all-trusting host name verifier
            HostnameVerifier allHostsValid = new HostnameVerifier() {
                public boolean verify(String hostname, SSLSession session) {
                    return true;
                }
            };

            // Install the all-trusting host verifier
            HttpsURLConnection.setDefaultHostnameVerifier(allHostsValid);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (KeyManagementException e) {
            e.printStackTrace();
        }
    }

    private Provider<ObjectMapper> objectMapperProvider = ObjectMapper::new;

    @Override
    protected void configure() {

        expose(Client.class);

        final Key<ContextResolver<ObjectMapper>> key = Key.get(new TypeLiteral<ContextResolver<ObjectMapper>>(){});

        final Provider<ContextResolver<ObjectMapper>> contextResolverProvider;

        contextResolverProvider = getProvider(key);

        bind(Client.class).toProvider(() -> newBuilder()
            .register(contextResolverProvider.get())
            .build())
        .asEagerSingleton();

        bind(ObjectMapper.class).toProvider(objectMapperProvider);
        bind(new TypeLiteral<ContextResolver<ObjectMapper>>(){}).to(ObjectMapperContextResolver.class);

    }

    /**
     * Specifies a {@link Provider<ObjectMapper>} which will be used to provide the underlying HTTP client's
     * {@link ObjectMapper}.
     *
     * @param objectMapperProvider the {@link Provider<ObjectMapper>}
     * @return this instance
     */
    public JacksonHttpClientModule withObjectMapperProvider(final Provider<ObjectMapper> objectMapperProvider) {
        this.objectMapperProvider = objectMapperProvider;
        return this;
    }

}
