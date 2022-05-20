package com.namazustudios.socialengine.docserve;

import java.nio.file.Path;

/**
 * Provides access to documentation via a path of static files on disk.
 */
public interface StaticPathDocs {

    /**
     * Returns the rooth path where the documentation is stored.
     * @return
     */
    Path getPath();

    /**
     * Starts up and loads all
     */
    void start();

}
