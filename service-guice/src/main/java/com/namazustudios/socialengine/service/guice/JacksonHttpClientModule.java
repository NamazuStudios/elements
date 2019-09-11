package com.namazustudios.socialengine.service.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Provider;
import javax.net.ssl.*;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.ext.ContextResolver;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.google.inject.Scopes.SINGLETON;
import static com.namazustudios.socialengine.annotation.ClientSerializationStrategy.DEFAULT;
import static javax.ws.rs.client.ClientBuilder.newBuilder;

public class JacksonHttpClientModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(JacksonHttpClientModule.class);

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

            logger.warn("Disabling SSL Certificate Validation.");

            final TrustManager[] trustAllCerts = new TrustManager[] {new X509TrustManager() {
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                    return null;
                }
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {}
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {}
                }
            };


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
            logger.error("Caught exception disabling SSL Verification.", e);
        } catch (KeyManagementException e) {
            logger.error("Caught exception disabling SSL Verification.", e);
        }
    }

    private List<Consumer<ClientBuilder>> registrations = new ArrayList<>();

    private Provider<ObjectMapper> defaultObjectMapperProvider = ObjectMapper::new;

    private Map<String, Provider<ObjectMapper>> namedObjectMapperProviders = new HashMap<>();

    @Override
    protected void configure() {

        expose(Client.class);

        final Key<ContextResolver<ObjectMapper>> key = Key.get(new TypeLiteral<ContextResolver<ObjectMapper>>(){});

        final Provider<ContextResolver<ObjectMapper>> contextResolverProvider;

        contextResolverProvider = getProvider(key);

        bind(Client.class).toProvider(() -> {

            final ClientBuilder builder = newBuilder()
                .register(JacksonFeature.class)
                .register(contextResolverProvider.get());

            registrations.forEach(c -> c.accept(builder));

            return builder.build();

        }).in(SINGLETON);

        bind(new TypeLiteral<ContextResolver<ObjectMapper>>(){}).to(ClientObjectMapperContextResolver.class);

        final MapBinder<String, ObjectMapper> stringObjectMapperMapBinder;
        stringObjectMapperMapBinder = MapBinder.newMapBinder(binder(), String.class, ObjectMapper.class);
        stringObjectMapperMapBinder.addBinding(DEFAULT).toProvider(defaultObjectMapperProvider);
        namedObjectMapperProviders.forEach((k, v) -> stringObjectMapperMapBinder.addBinding(k).toProvider(v));

    }

    /**
     * Adds a type to be passed to {@link ClientBuilder#register(Object)}.
     *
     * @param <T> the type to register
     * @return this instance
     */
    public <T> JacksonHttpClientModule withRegisteredComponent(final Class<T> cls) {
        registrations.add(cb -> cb.register(cls));
        return this;
    }

    /**
     * Specifies a {@link Provider<ObjectMapper>} which will be used to provide the underlying HTTP client's
     * {@link ObjectMapper}.
     *
     * @param objectMapperProvider the {@link Provider<ObjectMapper>}
     * @return this instance
     */
    public JacksonHttpClientModule withDefaultObjectMapperProvider(final Provider<ObjectMapper> objectMapperProvider) {
        this.defaultObjectMapperProvider = objectMapperProvider;
        return this;
    }

    /**
     * Specifies a {@link Provider<ObjectMapper>} which will be bound with the supplied name using the
     * {@link javax.inject.Named} annotation.
     *
     * @param name the name
     * @param objectMapperProvider the {@link Provider<ObjectMapper>}
     *
     * @return this instance
     */
    public JacksonHttpClientModule withNamedObjectMapperProvider(final String name,
                                                                 final Provider<ObjectMapper> objectMapperProvider) {
        namedObjectMapperProviders.put(name, objectMapperProvider);
        return this;
    }

}
