package com.namazustudios.socialengine.guice;

import com.google.common.base.Splitter;
import com.google.common.collect.Lists;
import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.google.inject.multibindings.Multibinder;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.nnsoft.guice.rocoto.converters.URIConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Properties;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.CORS_ALLOWED_ORIGINS;
import static com.namazustudios.socialengine.rt.Constants.STATIC_INSTANCE_INVOKER_ADDRESSES_NAME;
import static com.namazustudios.socialengine.rt.Constants.STATIC_INSTANCE_CONTROL_ADDRESSES_NAME;

/**
 * Created by patricktwohig on 4/3/15.
 */
public class ConfigurationModule extends AbstractModule {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationModule.class);

    private final Supplier<Properties> propertiesSupplier;

    public ConfigurationModule(final Supplier<Properties> propertiesSupplier) {
        this.propertiesSupplier = propertiesSupplier;
    }

    @Override
    protected void configure() {

        install(new URIConverter());
        install(new FileConverter());

        final Properties properties = propertiesSupplier.get();
        logger.info("Using configuration properties {} from {}", properties, propertiesSupplier.getClass().getName());
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

        final String invokerAddressesString = properties.getProperty(STATIC_INSTANCE_INVOKER_ADDRESSES_NAME, "");
        final Iterable<String> invokerAddressStringIterable = Splitter
                .on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(invokerAddressesString);
        final List<String> invokerAddresses = Lists.newArrayList(invokerAddressStringIterable);
        bind(new TypeLiteral<List<String>>(){})
                .annotatedWith(named(STATIC_INSTANCE_INVOKER_ADDRESSES_NAME))
                .toInstance(invokerAddresses);

        final String controlAddressesString = properties.getProperty(STATIC_INSTANCE_CONTROL_ADDRESSES_NAME, "");
        final Iterable<String> controlAddressStringIterable = Splitter
                .on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(controlAddressesString);
        final List<String> controlAddresses = Lists.newArrayList(controlAddressStringIterable);
        bind(new TypeLiteral<List<String>>(){})
                .annotatedWith(named(STATIC_INSTANCE_CONTROL_ADDRESSES_NAME))
                .toInstance(controlAddresses);
    }
}
