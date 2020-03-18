package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.rest.RestAPIMain.*;

public class RestJettyModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(BIND_ADDRESS, DEFAULT_BIND_ADDRESS);
        properties.put(PORT, Integer.toString(DEFAULT_PORT));
        properties.put(API_CONTEXT, DEFAULT_API_CONTEXT);
        return properties;
    }

}
