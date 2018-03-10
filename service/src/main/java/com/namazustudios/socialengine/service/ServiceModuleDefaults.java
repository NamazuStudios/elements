package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.namazustudios.socialengine.Constants.SESSION_TIMEOUT_SECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(SESSION_TIMEOUT_SECONDS, Long.toString(SECONDS.convert(48, TimeUnit.HOURS)));
        return properties;
    }

}
