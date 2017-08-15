package com.namazustudios.socialengine.rt;

import java.io.InputStream;

/**
 * Used to load the various assets for the underlying scripting engine.  This is responsible
 * for providing a raw access to the raw server-side assets.  This allows for the opening of
 * files relative to the root path of the application.
 *
 * This may or may not actually live on a real filesystem.  This can provide, for example,
 * access to an archive/zip file or a virtual file system.
 *
 * Created by patricktwohig on 8/14/17.
 */
public interface AssetLoader extends AutoCloseable {

    /**
     * Closes the {@link AssetLoader} and cleaning up any resources.  Any open {@link InputStream}
     * instances may be closed.
     */
    @Override
    void close();

    /**
     * Reads an asset as a String.  {@link #open(Path)}
     *
     * @param pathString the path string
     * @return
     */
    default InputStream open(final String pathString) {
        final Path path = new Path(pathString);
        return open(path);
    }

    /**
     * Opens a {@link InputStream} to an asset on the application's path.
     *
     * @param path the {@link Path} to the file.
     * @return an {@link InputStream}
     */
    InputStream open(Path path);

}
