package dev.getelements.elements.dao;

import dev.getelements.elements.exception.NotFoundException;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Provides an interface allowing access to a filesystem permitting the reading and writing of
 * larger files.
 *
 * Created by patricktwohig on 6/29/17.
 */
public interface FileDao {

    /**
     * Opens an {@link InputStream} to read file at the path.  Throws the appropriate exception
     * if the file cannot be read (eg {@link NotFoundException}.)
     *
     * It should go without saying the returned {@link InputStream} must be closed properly for
     * the the operation to complete fully.  Failing to close a stream results in undefined
     * behavior.
     *
     * @param path the path to the file
     * @return the {@link InputStream} used to write the file
     */
    InputStream readFile(final String path);

    /**
     * Opens an {@link OutputStream} to write a file at the supplied path.  If a file already
     * exists at the supplied path, then this will replace the existing file.  No guarantees
     * can be made about a file being
     *
     * It should go without saying the returned {@link OutputStream} must be closed properly for
     * the the operation to complete fully.  Failing to close a stream results in undefined
     * behavior.
     *
     * @param path
     * @return
     */
    OutputStream writeFile(final String path);

}
