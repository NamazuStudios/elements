package dev.getelements.elements.rt.jersey.guice;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.MapBinder;
import dev.getelements.elements.rt.jersey.ClientObjectMapperContextResolver;
import dev.getelements.elements.rt.jersey.GenericMultipartFeature;
import dev.getelements.elements.rt.jersey.OctetStreamJsonMessageBodyReader;
import dev.getelements.elements.rt.util.AppleDateFormat;
import org.glassfish.jersey.jackson.JacksonFeature;
import org.glassfish.jersey.media.multipart.MultiPartFeature;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Provider;
import javax.net.ssl.*;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.ClientBuilder;
import jakarta.ws.rs.ext.ContextResolver;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static com.fasterxml.jackson.databind.DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES;
import static com.fasterxml.jackson.databind.PropertyNamingStrategies.SNAKE_CASE;
import static com.google.inject.Scopes.SINGLETON;
import static dev.getelements.elements.rt.annotation.ClientSerializationStrategy.*;
import static jakarta.ws.rs.client.ClientBuilder.newBuilder;

public class JerseyHttpClientModule extends PrivateModule {

    private static final Logger logger = LoggerFactory.getLogger(JerseyHttpClientModule.class);

    public static final String DISABLE_SSL_VERIFICATION = "dev.getelements.ssl.verification.disable";

    static {

        final String disable = System.getProperty(DISABLE_SSL_VERIFICATION);

        if (Boolean.parseBoolean(disable)) {
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

        withRegisteredComponent(OctetStreamJsonMessageBodyReader.class);

        withDefaultObjectMapperProvider(() -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        });

        withNamedObjectMapperProvider(APPLE_ITUNES, () -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            final DateFormat dateFormat = new AppleDateFormat();
            objectMapper.setDateFormat(dateFormat);
            objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
            objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        });

        withNamedObjectMapperProvider(META_GRAPH, () -> {
            final ObjectMapper objectMapper = new ObjectMapper();
            objectMapper.setPropertyNamingStrategy(SNAKE_CASE);
            objectMapper.configure(FAIL_ON_UNKNOWN_PROPERTIES, false);
            return objectMapper;
        });

        final Key<ContextResolver<ObjectMapper>> key = Key.get(new TypeLiteral<ContextResolver<ObjectMapper>>(){});
        final Provider<ContextResolver<ObjectMapper>> contextResolverProvider = getProvider(key);

        bind(Client.class).toProvider(() -> {

            final ClientBuilder builder = newBuilder()
                .register(JacksonFeature.class)
                .register(MultiPartFeature.class)
                .register(GenericMultipartFeature.class)
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
    public <T> JerseyHttpClientModule withRegisteredComponent(final Class<T> cls) {
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
    public JerseyHttpClientModule withDefaultObjectMapperProvider(final Provider<ObjectMapper> objectMapperProvider) {
        this.defaultObjectMapperProvider = objectMapperProvider;
        return this;
    }

    /**
     * Specifies a {@link Provider<ObjectMapper>} which will be bound with the supplied name using the
     * {@link jakarta.inject.Named} annotation.
     *
     * @param name the name
     * @param objectMapperProvider the {@link Provider<ObjectMapper>}
     *
     * @return this instance
     */
    public JerseyHttpClientModule withNamedObjectMapperProvider(final String name,
                                                                final Provider<ObjectMapper> objectMapperProvider) {
        namedObjectMapperProviders.put(name, objectMapperProvider);
        return this;
    }

}
