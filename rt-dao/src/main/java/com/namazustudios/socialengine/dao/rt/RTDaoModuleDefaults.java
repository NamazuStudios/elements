package com.namazustudios.socialengine.dao.rt;

import com.namazustudios.socialengine.ModuleDefaults;

import java.util.Properties;

import static com.namazustudios.socialengine.Constants.GIT_STORAGE_DIRECTORY;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTDaoModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.put(GIT_STORAGE_DIRECTORY, "repositories");
        return properties;
    }

}
