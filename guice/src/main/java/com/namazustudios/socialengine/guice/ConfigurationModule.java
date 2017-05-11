package com.namazustudios.socialengine.guice;

import com.google.inject.AbstractModule;
import com.google.inject.name.Names;
import org.nnsoft.guice.rocoto.converters.URIConverter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;
import java.util.function.Supplier;

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

        final Properties properties = propertiesSupplier.get();
        logger.info("Using configuration properties " + properties);
        Names.bindProperties(binder(), properties);

    }

}
