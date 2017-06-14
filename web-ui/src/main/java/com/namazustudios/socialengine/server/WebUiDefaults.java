package com.namazustudios.socialengine.server;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.ModuleDefaults;

import java.util.Properties;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class WebUiDefaults implements ModuleDefaults {
    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties();
        defaultProperties.setProperty(Constants.API_OUTSIDE_URL, "http://localhost:8080/api/rest");
        return defaultProperties;
    }
}
