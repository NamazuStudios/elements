package com.namazustudios.socialengine.codeserve;

import com.namazustudios.socialengine.config.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.HTTP_PATH_PREFIX;
import static com.namazustudios.socialengine.Constants.HTTP_PORT;
import static com.namazustudios.socialengine.rt.git.Constants.GIT_STORAGE_DIRECTORY;

/**
 * Module defaults for the code server.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class CodeServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(HTTP_PORT, "8082");
        properties.put(HTTP_PATH_PREFIX, "code");
        properties.put(GIT_STORAGE_DIRECTORY, "script-repos/git");
        return properties;
    }

}
