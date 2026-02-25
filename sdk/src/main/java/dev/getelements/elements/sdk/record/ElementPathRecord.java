package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementPathLoader;

import java.nio.file.Path;
import java.util.List;

/**
 * A record type containing the bundle information for an Element. This breaks down the internals of the element
 * deployment on disk.
 *
 * @param path the root path for the Element
 * @param api the contents of the {@link ElementPathLoader#API_DIR} underneath the {@link #path()}
 * @param spi the contents of the {@link ElementPathLoader#SPI_DIR} underneath the {@link #path()}
 * @param lib the contents of the {@link ElementPathLoader#LIB_DIR} underneath the {@link #path()}
 * @param classpath the contents of the {@link ElementPathLoader#LIB_DIR} underneath the {@link #path()}
 * @param attributes the attributes contained in the bundle
 * @param manifest the manifest from the bundle
 */
public record ElementPathRecord(
        Path path,
        List<Path> api,
        List<Path> spi,
        List<Path> lib,
        List<Path> classpath,
        Attributes attributes,
        ElementManifestRecord manifest
) {

    /**
     * Returns a copy of this {@link ElementPathRecord} relative to the value of {@link #path()}. Throwing an
     * {@link IllegalStateException} if any of the contained paths are not part of the parent path. Additionally, this
     * shortens the root path to just its filename.
     *
     * @return a relativized {@link ElementPathRecord}
     */
    public ElementPathRecord relativize() {
        return new ElementPathRecord(
            path.getFileName(),
            api.stream().map(this::relativize).toList(),
            spi.stream().map(this::relativize).toList(),
            lib.stream().map(this::relativize).toList(),
            classpath.stream().map(this::relativize).toList(),
            attributes,
            manifest
        );
    }

    private Path relativize(final Path p) {

        if (!p.startsWith(path)) {
            throw new IllegalStateException("Path " + p + " is not under root " + path);
        }

        return path.relativize(p);
    }

}
