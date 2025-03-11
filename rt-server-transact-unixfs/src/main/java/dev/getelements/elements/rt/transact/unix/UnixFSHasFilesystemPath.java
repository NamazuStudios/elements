package dev.getelements.elements.rt.transact.unix;

import java.io.IOException;
import java.nio.file.Path;

import static dev.getelements.elements.rt.transact.unix.UnixFSUtils.TRANSACTION_EXTENSION;
import static java.lang.String.format;
import static java.nio.file.Files.createDirectories;

/**
 * Used to represent an object which has a FS path component.
 */
public interface UnixFSHasFilesystemPath {
    /**
     * Gets the FS Path for the object.
     *
     * @return
     */
    Path getFilesystemPath();

    /**
     * Gets the FS Path for hte object, appending the transaction ID.
     *
     * @param transactionId the transaction ID
     * @return the {@link Path} appending the transaction ID.
     */
    default Path getFilesystemPath(String transactionId) {

        final var filename = getFilesystemPath().getFileName();

        return getFilesystemPath().resolveSibling(format(
                "%s.%s.%s",
                filename,
                transactionId,
                TRANSACTION_EXTENSION)
        );

    }

    /**
     * Creates all directories for this instance.
     *
     * @return this instance
     * @throws IOException
     */
    default UnixFSHasFilesystemPath createParentDirectories() throws IOException {
        createDirectories(getFilesystemPath().getParent());
        return this;
    }

}

