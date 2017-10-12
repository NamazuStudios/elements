package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(HTTP_TUNNEL_PORT, "8080");
        return properties;
    }

}
