package dev.getelements.elements.sdk.record;

import java.nio.file.Path;

/**
 * Represents an artifact.
 *
 * @param path the path to the artifact
 * @param extension the type or file extension
 *
 */
public record Artifact(Path path, String extension) {}
