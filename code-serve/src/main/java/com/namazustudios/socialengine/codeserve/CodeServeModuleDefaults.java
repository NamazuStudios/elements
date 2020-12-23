package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;

/**
 * Module defaults for the code server.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class CodeServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "repositories");
        return properties;
    }

}
