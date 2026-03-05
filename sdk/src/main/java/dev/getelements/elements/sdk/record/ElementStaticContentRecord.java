package dev.getelements.elements.sdk.record;

import java.nio.file.Path;
import java.util.List;

/**
 * Represents a path of static content provided at the supplied root, listing all available contents.
 *
 * @param root the root directory
 * @param contents the contents
 * @since 3.8
 */
public record ElementStaticContentRecord(Path root, List<Path> contents) {

    /**
     * Converts this to relative paths.
     *
     * @return the static content record as relative paths.
     */
    public ElementStaticContentRecord relativize() {
        return new ElementStaticContentRecord(
                root,
                contents.stream().map(this::relativize).toList()
        );
    }

    private Path relativize(final Path p) {

        if (!p.startsWith(root)) {
            throw new IllegalStateException("Path " + p + " is not under root " + root);
        }

        return root.relativize(p);
    }

}
