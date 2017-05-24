package com.namazustudios.socialengine.server.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.DefaultConfiguration;

import java.util.Properties;

import static com.google.inject.name.Names.bindProperties;

/**
 * Created by patricktwohig on 5/11/17.
 */
public class UiGuiceConfigurationModule extends AbstractModule {

    @Override
    protected void configure() {
        final DefaultConfiguration defaultConfiguration = new DefaultConfiguration();
        final Properties properties = defaultConfiguration.get();
        bindProperties(binder(), properties);
    }

}
