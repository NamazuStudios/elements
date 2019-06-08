package com.namazustudios.socialengine.guice;

import com.google.common.base.Splitter;
import com.google.inject.AbstractModule;
import com.google.inject.multibindings.Multibinder;
import org.nnsoft.guice.rocoto.converters.FileConverter;
import org.nnsoft.guice.rocoto.converters.URIConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Properties;
import java.util.function.Supplier;

import static com.google.inject.multibindings.Multibinder.newSetBinder;
import static com.google.inject.name.Names.bindProperties;
import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.Constants.CORS_ALLOWED_ORIGINS;
import static com.namazustudios.socialengine.rt.Constants.LOCAL_INSTANCE_CONNECT_PORTS_NAME;
import static com.namazustudios.socialengine.rt.Constants.LOCAL_INSTANCE_CONTROL_PORTS_NAME;

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

        final Multibinder<Integer> connectPortsMultibinder = newSetBinder(binder(), Integer.class, named(LOCAL_INSTANCE_CONNECT_PORTS_NAME));
        final String connectPortsString = properties.getProperty(LOCAL_INSTANCE_CONNECT_PORTS_NAME, "");
        final Iterable<String> connectPortStringIterable = Splitter
                .on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(connectPortsString);
        for (final String connectPortString : connectPortStringIterable) {
            try {
                connectPortsMultibinder.addBinding().toInstance(new Integer(connectPortString));
            }
            catch (Exception e) {
                binder().addError(e);
            }
        }

        final Multibinder<Integer> controlPortsMultibinder = newSetBinder(binder(), Integer.class, named(LOCAL_INSTANCE_CONTROL_PORTS_NAME));
        final String controlPortsString = properties.getProperty(LOCAL_INSTANCE_CONTROL_PORTS_NAME, "");
        final Iterable<String> controlPortStringIterable = Splitter
                .on(",")
                .trimResults()
                .omitEmptyStrings()
                .split(controlPortsString);
        for (final String controlPortString : controlPortStringIterable) {
            try {
                controlPortsMultibinder.addBinding().toInstance(new Integer(controlPortString));
            }
            catch (Exception e) {
                binder().addError(e);
            }
        }
    }
}
