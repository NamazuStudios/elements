package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.exception.AssetNotFoundException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * An instance of {@link AssetLoader} which operates on a path in the filesystem.
 *
 * Created by patricktwohig on 8/14/17.
 */
public class FileAssetLoader implements AssetLoader {

    private static final Logger logger = LoggerFactory.getLogger(FileAssetLoader.class);

    private final File rootDirectory;

    private final AtomicBoolean open = new AtomicBoolean(true);

    private final AtomicInteger openStreams = new AtomicInteger();

    public FileAssetLoader(final File rootDirectory) {

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        } else if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory + " must be a directory.");
        }

        this.rootDirectory = rootDirectory;

    }

    @Override
    public void close() {

        final int count = openStreams.get();

        if (!open.compareAndSet(true, false)) {
            logger.error("Already closed this instance {}.  Streams open: {}", this, count);
        } else if (count != 0) {
            logger.error("Cosing this instance {}.  Streams open: {}", this, count);
        } else {
            logger.info("Closing {}", this);
        }

    }

    @Override
    public InputStream open(final Path path) {

        if (!open.get()) {
            throw new InternalException("asset loader not open.");
        }

        if (path.isWildcard()) {
            throw new IllegalArgumentException(path + " must not be a wildcard.");
        }

        final String normalizedPath = path.toFileSystemPathString();
        final File absolute = new File(getRootDirectory(), normalizedPath).getAbsoluteFile();

        final FileInputStream fis;

        try {
            fis = new FileInputStream(absolute);
        } catch (FileNotFoundException ex) {
            throw new AssetNotFoundException(ex);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

        openStreams.incrementAndGet();

        final BufferedInputStream bis = new BufferedInputStream(fis) {

            final AtomicBoolean closed = new AtomicBoolean(false);

            @Override
            public void close() throws IOException {

                if (closed.compareAndSet(true, false)) {
                    openStreams.decrementAndGet();
                }

                super.close();

            }
        };

        return bis;

    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public String toString() {
        return "FileAssetLoader{" + "rootDirectory=" + rootDirectory + '}';
    }

}
