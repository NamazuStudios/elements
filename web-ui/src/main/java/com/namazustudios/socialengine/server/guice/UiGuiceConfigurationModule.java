package com.namazustudios.socialengine.server.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.SystemPropertiesConfiguration;

import java.util.Properties;

import static com.google.inject.name.Names.bindProperties;

/**
 * Created by patricktwohig on 5/11/17.
 */
public class UiGuiceConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        final SystemPropertiesConfiguration systemPropertiesConfiguration = new SystemPropertiesConfiguration();
        final Properties properties = systemPropertiesConfiguration.get();
        bindProperties(binder(), properties);
    }

}
