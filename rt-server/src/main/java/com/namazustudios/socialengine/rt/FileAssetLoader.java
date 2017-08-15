package com.namazustudios.socialengine.rt;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 8/14/17.
 */
public class FileAssetLoader implements AssetLoader {

    private final File rootDirectory;

    public FileAssetLoader(final File rootDirectory) {

        if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory + " must be a directory.");
        }

        this.rootDirectory = rootDirectory;

    }

    @Override
    public void close() {}

    @Override
    public InputStream open(Path path) throws IOException {

        if (path.isWildcard()) {
            throw new IllegalArgumentException(path + " must not be a wildcard.");
        }

        return new FileInputStream(path.toNormalizedPathString(File.pathSeparator));

    }

}
