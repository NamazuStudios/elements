package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.config.ModuleDefaults;
import com.namazustudios.socialengine.dao.mongo.provider.MongoGridFSFileDaoProvider;

import java.util.Properties;

/**
 * Created by patricktwohig on 6/29/17.
 */
public class MongoFileModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties properties = new Properties();
        properties.setProperty(MongoGridFSFileDaoProvider.FILE_SERVICE_BUCKET, "socialengine-files");
        return properties;
   }

}
