package com.namazustudios.socialengine.rest;

import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.ModuleDefaults;

import java.util.Properties;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class RestApiModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties();
        defaultProperties.setProperty(Constants.SHORT_LINK_BASE, "http://localhost:8888/l");
        defaultProperties.setProperty(Constants.QUERY_MAX_RESULTS, Integer.valueOf(20).toString());
        defaultProperties.setProperty(Constants.PASSWORD_DIGEST_ALGORITHM, "SHA-256");
        defaultProperties.setProperty(Constants.PASSWORD_ENCODING, "UTF-8");
        defaultProperties.setProperty(Constants.API_PREFIX, "rest");
        defaultProperties.setProperty(Constants.API_OUTSIDE_URL, "http://localhost:8080/api/rest");
        defaultProperties.setProperty(Constants.CORS_ALLOWED_ORIGINS, "http://localhost:8888, http://127.0.0.1:8888");
        defaultProperties.setProperty(Constants.ASYNC_TIMEOUT_LIMIT, Integer.toString(0));
        return defaultProperties;
    }
}
