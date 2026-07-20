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
     * <p>{@code allPropertiesSupplier} is used for both {@code @Named} Guice bindings and
     * {@code SYSTEM_ATTRIBUTES}. It must include classpath-scanned {@code @ElementDefaultAttribute}
     * defaults so that elements can read any server default via {@code @Named} injection.
     * Elements override individual keys by re-declaring them with {@code @ElementDefaultAttribute}.</p>
     *
     * <p>{@code systemAttributesSupplier} is used exclusively for {@code GLOBAL_ELEMENT_ATTRIBUTES}
     * and should return <em>only</em> operator-set values (env vars, system properties, config files).
     * This layer sits above element {@code @ElementDefaultAttribute} declarations in the priority
     * chain, so operator overrides win over element defaults without per-element path configuration.</p>
     *
     * @param allPropertiesSupplier      full merged properties (scan defaults + operator-set)
     * @param systemAttributesSupplier   explicit-only operator properties for {@code GLOBAL_ELEMENT_ATTRIBUTES}
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
        final Properties explicitProperties = systemAttributesSupplier.get();

        if (properties == null) {
            addError("Supplier supplied null properties.");
        }

        // SYSTEM_ATTRIBUTES is the floor for element attribute resolution. It includes ALL
        // system properties (classpath-scanned @ElementDefaultAttribute scan defaults plus
        // operator-set values) so elements can read any server default via @Named injection.
        // Elements override individual keys by re-declaring them with @ElementDefaultAttribute.
        final var allSystemAttrsBuilder = new SimpleAttributes.Builder();
        if (properties != null) {
            for (Enumeration<?> e = properties.propertyNames(); e.hasMoreElements();) {
                final var name = e.nextElement().toString();
                allSystemAttrsBuilder.setAttribute(name, properties.getProperty(name));
            }
        }
        final var allSystemAttributes = allSystemAttrsBuilder.build().immutableCopy();

        // GLOBAL_ELEMENT_ATTRIBUTES holds only operator-set explicit values (env vars, system
        // properties, config files). Applied above element @ElementDefaultAttribute in the
        // priority chain so operator overrides win over element-declared defaults, but are
        // themselves overridable by per-element path attributes.
        final var explicitAttrsBuilder = new SimpleAttributes.Builder();
        if (explicitProperties != null) {
            for (Enumeration<?> e = explicitProperties.propertyNames(); e.hasMoreElements();) {
                final var name = e.nextElement().toString();
                explicitAttrsBuilder.setAttribute(name, explicitProperties.getProperty(name));
            }
        }
        final var explicitAttributes = explicitAttrsBuilder.build().immutableCopy();

        bind(Attributes.class)
                .annotatedWith(named(Attributes.SYSTEM_ATTRIBUTES))
                .toInstance(allSystemAttributes);

        bind(Attributes.class)
                .annotatedWith(named(Attributes.GLOBAL_ELEMENT_ATTRIBUTES))
                .toInstance(explicitAttributes);

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
