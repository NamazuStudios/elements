package com.namazustudios.socialengine.rt.transact.unix;

import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.transact.Revision;
import com.namazustudios.socialengine.rt.transact.RevisionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.io.IOException;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.function.Function;

import static com.namazustudios.socialengine.rt.transact.Revision.infinity;
import static java.lang.String.format;
import static java.nio.file.Files.deleteIfExists;
import static java.nio.file.Files.isDirectory;
import static java.util.Comparator.naturalOrder;

public class UnixFSUtils {

    private static final Logger logger = LoggerFactory.getLogger(UnixFSUtils.class);

    public static final String LOCK_FILE_NAME = "lock";

    private final RevisionFactory revisionFactory;

    @Inject
    public UnixFSUtils(final RevisionFactory revisionFactory) {
        this.revisionFactory = revisionFactory;
    }

    public Path lockDirectory(final Path directoryPath) throws IOException {

        final Path path = Paths.get(".",LOCK_FILE_NAME);
        final Path lockFilePath = directoryPath.toAbsolutePath().resolve(path).toAbsolutePath();

        if (Files.exists(lockFilePath)) {
            final String msg = format("Journal path is locked %s", lockFilePath);
            throw new FileAlreadyExistsException(msg);
        } else {
            Files.createFile(lockFilePath);
        }

        return lockFilePath;

    }

    public void unlockDirectory(final Path lockFilePath) {
        try {
            deleteIfExists(lockFilePath);
        } catch (IOException e) {
            logger.error("Failed to delete lock file.", e);
        }
    }

    public Path resolve(final Path directory,  final Revision<?> revision) {

        if (!isDirectory(directory)) throw new IllegalArgumentException(directory + " must be a directory.");

        return doOperation(() -> Files
                .list(directory)
                .filter(Files::isRegularFile)
                .map(file -> revisionFactory.create(file.getFileName().toString(), file))
                .filter(r -> r.isBeforeOrSame(revision))
                .max(naturalOrder())
                .orElse(infinity()))
                .getValue().orElseThrow(() -> new InternalException("No suitable combination of " + revision + " " +
                                                                    "and filesystem path " + directory));

    }

    public <T> T doOperation(final IOOperation<T> action) {
        try {
            return action.perform();
        } catch (Exception ex) {
            logger.error("IOException Performing operation.", ex);
            throw new InternalException(ex);
        }
    }

    @FunctionalInterface
    public interface IOOperation<T> {

        T perform() throws IOException;

    }

}
