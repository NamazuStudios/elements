package dev.getelements.elements.cdnserve;

import dev.getelements.elements.config.ModuleDefaults;

import java.util.Properties;

import static dev.getelements.elements.Constants.*;
import static dev.getelements.elements.rt.git.Constants.GIT_STORAGE_DIRECTORY;

/**
 * Module defaults for the content server.
 *
 * Created by garrettmcspadden on 12/21/20.
 */
public class CdnServeModuleDefaults implements ModuleDefaults {

    @Override
    public Properties get() {
        final var properties = new Properties();
        properties.put(CDN_FILE_DIRECTORY, "content");
        properties.put(CDN_CLONE_ENDPOINT, "clone");
        properties.put(CDN_SERVE_ENDPOINT, "serve");
        properties.put(GIT_STORAGE_DIRECTORY, "cdn-repos/git");
        return properties;
    }

}
