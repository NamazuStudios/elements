package com.namazustudios.socialengine.cdnserve;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.*;

/**
 * Module defaults for the content server.
 *
 * Created by garrettmcspadden on 12/21/20.
 */
public class CdnServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "repositories");
        properties.put(HTTP_TUNNEL_PORT, "8084");
        properties.put(CDN_FILE_DIRECTORY, "content");
        properties.put(CDN_CLONE_ENDPOINT, "clone");
        properties.put(CDN_SERVE_ENDPOINT, "serve");
        return properties;
    }

}
