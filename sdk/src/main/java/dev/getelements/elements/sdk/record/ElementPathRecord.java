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
) {}
