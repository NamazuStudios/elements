package com.namazustudios.socialengine.service;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;
import java.util.concurrent.TimeUnit;

import static com.namazustudios.socialengine.Constants.*;
import static com.namazustudios.socialengine.Constants.NEO_BLOCKCHAIN_PORT;
import static java.util.concurrent.TimeUnit.SECONDS;

public class ServiceModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(SESSION_TIMEOUT_SECONDS, Long.toString(SECONDS.convert(48, TimeUnit.HOURS)));
        properties.put(MOCK_SESSION_TIMEOUT_SECONDS, Long.toString(SECONDS.convert(1, TimeUnit.HOURS)));
        properties.put(NEO_BLOCKCHAIN_HOST, "http://127.0.0.1");
        properties.put(NEO_BLOCKCHAIN_PORT, "20332");
        return properties;
    }

}
