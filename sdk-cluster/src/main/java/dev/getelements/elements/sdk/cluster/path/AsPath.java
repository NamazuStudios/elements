package dev.getelements.elements.sdk.cluster.path;

import dev.getelements.elements.sdk.cluster.path.exception.InvalidPathException;

import java.util.Optional;

/**
 * Specifies a type which can be represented as a {@link Path}.
 */
public interface AsPath {

    /**
     * Returns this object as a {@link Path}, throws an instance of {@link InvalidPathException} if the path can't
     * be determined
     * @return the path, never null
     * @throws InvalidPathException if the path contained therein is invalid
     */
    default Path path() throws InvalidPathException {
        return findPath().orElseThrow(InvalidPathException::new);
    }

    /**
     * Finds the {@link Path} or returns an empty {@link Optional} if no path was found.
     *
     * @return the path optional
     */
    default Optional<Path> findPath() {
        try {
            return Optional.of(path());
        } catch (InvalidPathException ex) {
            return Optional.empty();
        }
    }

}
