package com.namazustudios.socialengine.setup;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

/**
 * Created by patricktwohig on 6/14/17.
 */
public class SetupDefaults implements ModuleDefaults {
    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties();
        defaultProperties.setProperty(Constants.QUERY_MAX_RESULTS, "100");
        defaultProperties.setProperty(Constants.PASSWORD_DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(Constants.PASSWORD_ENCODING, "UTF-8");
        return defaultProperties;
    }
}
