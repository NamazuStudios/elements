package dev.getelements.elements.guice;

import com.google.common.base.Splitter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.nnsoft.guice.rocoto.converters.URIConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Enumeration;
import java.util.Properties;
import java.util.function.Supplier;

import static com.google.inject.TypeLiteral.get;
import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.model.Constants.CORS_ALLOWED_ORIGINS;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class ConfigurationModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    private final Supplier<Properties> propertiesSupplier;

    private final Supplier<Properties> systemAttributesSupplier;

    /**
     * Uses all properties (including classpath-scanned {@code @ElementDefaultAttribute} defaults)
     * for both {@code @Named} bindings and {@code SYSTEM_ATTRIBUTES}.  Suitable for non-element
     * contexts such as MongoDB test modules where element default isolation is not required.
     *
     * @param propertiesSupplier source of all merged properties
     */
    public ConfigurationModule(final Supplier<Properties> propertiesSupplier) {
        this.propertiesSupplier = propertiesSupplier;
        this.systemAttributesSupplier = propertiesSupplier;
    }

    /**
     * Preferred constructor for production use with {@code DefaultConfigurationSupplier}.
     *
     * <p>{@code allPropertiesSupplier} is used for {@code @Named} Guice bindings and must include
     * classpath-scanned {@code @ElementDefaultAttribute} defaults so that server infrastructure
     * constants (e.g. {@code session.timeout.seconds}) resolve correctly.</p>
     *
     * <p>{@code systemAttributesSupplier} is used exclusively for {@code SYSTEM_ATTRIBUTES} and
     * should return <em>only</em> operator-set values (env vars, system properties, config files).
     * This prevents server-level scan defaults from shadowing an element's own
     * {@code @ElementDefaultAttribute} re-declaration.</p>
     *
     * @param allPropertiesSupplier      full merged properties for {@code @Named} bindings
     * @param systemAttributesSupplier   explicit-only properties for {@code SYSTEM_ATTRIBUTES}
     */
    public ConfigurationModule(final Supplier<Properties> allPropertiesSupplier,
                               final Supplier<Properties> systemAttributesSupplier) {
        this.propertiesSupplier = allPropertiesSupplier;
        this.systemAttributesSupplier = systemAttributesSupplier;
    }

    @Override
    protected void configure() {

        install(new URIConverter());
        install(new FileConverter());
        convertToTypes(only(get(Path.class)), (s, to) -> Paths.get(s));

        final Properties properties = propertiesSupplier.get();
        final Properties systemAttributesProperties = systemAttributesSupplier.get();
        final SimpleAttributes.Builder systemAttributesBuilder = new SimpleAttributes.Builder();

        if (properties == null) {
            addError("Supplier supplied null properties.");
        } else {
            for (Enumeration<?> e = systemAttributesProperties.propertyNames(); e.hasMoreElements();) {
                final var name = e.nextElement().toString();
                final var value = systemAttributesProperties.getProperty(name);
                systemAttributesBuilder.setAttribute(name, value);
            }
        }

        final var systemAttributes = systemAttributesBuilder
                .build()
                .immutableCopy();

        bind(Attributes.class)
                .annotatedWith(named(Attributes.SYSTEM_ATTRIBUTES))
                .toInstance(systemAttributes);

        // GLOBAL_ELEMENT_ATTRIBUTES holds the same explicit operator-set values but is applied
        // inside the per-element attributes layer (above element @ElementDefaultAttribute, below
        // per-element path attributes). This gives operator-set properties priority over element
        // declared defaults without requiring per-element path attribute configuration.
        bind(Attributes.class)
                .annotatedWith(named(Attributes.GLOBAL_ELEMENT_ATTRIBUTES))
                .toInstance(systemAttributes);

        bind(Properties.class).toProvider(() -> new Properties(properties));

        logger.debug("Using configuration properties {} from {}", properties, propertiesSupplier.getClass().getName());
        bindProperties(binder(), properties);

        final Multibinder<URI> corsAllowedOriginsMultibinder;
        corsAllowedOriginsMultibinder = newSetBinder(binder(), URI.class, named(CORS_ALLOWED_ORIGINS));

        final String corsAllowedOriginsProperty = properties.getProperty(CORS_ALLOWED_ORIGINS, "");

        final Iterable<String> corsAllowedOrigins = Splitter
            .on(",")
            .trimResults()
            .omitEmptyStrings()
            .split(corsAllowedOriginsProperty);

        for (final String origin : corsAllowedOrigins) {
            try {
                corsAllowedOriginsMultibinder.addBinding().toInstance(new URI(origin));
            } catch (URISyntaxException e) {
                binder().addError(e);
            }
        }

    }

}
