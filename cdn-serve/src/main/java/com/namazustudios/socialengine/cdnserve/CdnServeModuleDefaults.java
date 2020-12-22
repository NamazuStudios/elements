package com.namazustudios.socialengine.cdnserve;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;

/**
 * Module defaults for the content server.
 *
 * Created by garrettmcspadden on 12/21/20.
 */
public class CdnServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "cdn.repositories");
        return properties;
    }

}
