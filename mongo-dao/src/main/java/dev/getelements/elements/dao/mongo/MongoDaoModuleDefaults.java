package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDaoModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final Properties defaultProperties = new Properties(System.getProperties());
        return defaultProperties;
    }

}
