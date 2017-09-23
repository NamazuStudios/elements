package com.namazustudios.socialengine.appserve;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

public class AppServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(Constants.HTTP_TUNNEL_PORT, "8080");
        return properties;
    }

}
