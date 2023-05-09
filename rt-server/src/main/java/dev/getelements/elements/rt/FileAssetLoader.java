package dev.getelements.elements.rt;

import dev.getelements.elements.rt.exception.AssetNotFoundException;
import dev.getelements.elements.rt.exception.InternalException;
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
public class FileAssetLoader extends AbstractAssetLoader {

    private static final Logger logger = LoggerFactory.getLogger(FileAssetLoader.class);

    private final File rootDirectory;

    public FileAssetLoader(final String rootDirectory) {
        this(new File(rootDirectory));
    }

    public FileAssetLoader(final File rootDirectory) {

        if (!rootDirectory.exists()) {
            rootDirectory.mkdirs();
        } else if (!rootDirectory.isDirectory()) {
            throw new IllegalArgumentException(rootDirectory + " must be a directory.");
        }

        this.rootDirectory = rootDirectory;

    }

    @Override
    protected InputStream doOpen(Path path) {

        final String normalizedPath = path.toFileSystemPathString();
        final File absolute = new File(getRootDirectory(), normalizedPath).getAbsoluteFile();

        try {
            return new FileInputStream(absolute);
        } catch (FileNotFoundException ex) {
            throw new AssetNotFoundException(ex);
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    }

    public File getRootDirectory() {
        return rootDirectory;
    }

    @Override
    public String toString() {
        return "FileAssetLoader{" + "rootDirectory=" + rootDirectory + '}';
    }

}
