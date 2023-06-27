package dev.getelements.elements.codeserve;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.rt.git.Constants.GIT_SCRIPT_STORAGE_DIRECTORY;

/**
 * Module defaults for the code server.
 *
 * Created by patricktwohig on 8/2/17.
 */
public class CodeServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(GIT_SCRIPT_STORAGE_DIRECTORY, "script-repos/git");
        return properties;
    }

}
