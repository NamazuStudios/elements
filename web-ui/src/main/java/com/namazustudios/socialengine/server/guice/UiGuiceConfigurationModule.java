package com.namazustudios.socialengine.server.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;

import java.util.Properties;
import java.util.function.Supplier;

import static com.google.inject.name.Names.bindProperties;

/**
 * Created by patricktwohig on 5/11/17.
 */
public class UiGuiceConfigurationModule extends AbstractModule {

    private final Supplier<ClassLoader> classLoaderSupplier;

    public UiGuiceConfigurationModule() {
        this(UiGuiceConfigurationModule.class::getClassLoader);
    }

    public UiGuiceConfigurationModule(Supplier<ClassLoader> classLoaderSupplier) {
        this.classLoaderSupplier = classLoaderSupplier;
    }

    @Override
    protected void configure() {
        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier(classLoaderSupplier.get());
        final Properties properties = defaultConfigurationSupplier.get();
        bindProperties(binder(), properties);
    }

}
