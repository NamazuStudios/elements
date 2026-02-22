package dev.getelements.elements.sdk.record;

import dev.getelements.elements.sdk.exception.SdkException;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Represents a resolved Maven artifact with its coordinates and local file system path.
 * <p>
 * This record encapsulates both the Maven coordinates (groupId:artifactId:version:packaging:classifier)
 * and the resolved local path to the artifact file. It is typically used as the result of artifact
 * resolution operations performed by {@link dev.getelements.elements.sdk.ElementArtifactLoader}.
 * <p>
 * The coordinates follow standard Maven naming conventions:
 * <ul>
 *   <li><b>group</b>: Maven groupId (e.g., "org.example")</li>
 *   <li><b>id</b>: Maven artifactId (e.g., "my-library")</li>
 *   <li><b>version</b>: Version string (e.g., "1.0.0")</li>
 *   <li><b>packaging</b>: Packaging type (e.g., "jar", "war", "pom")</li>
 *   <li><b>classifier</b>: Optional classifier (e.g., "sources", "javadoc"), may be null</li>
 *   <li><b>extension</b>: File extension, typically matches packaging but may differ</li>
 * </ul>
 *
 * @param path       the local file system path to the resolved artifact file
 * @param group      the Maven groupId
 * @param id         the Maven artifactId
 * @param version    the artifact version
 * @param packaging  the packaging type (jar, war, pom, etc.)
 * @param classifier the optional classifier (may be null)
 * @param extension  the file extension
 */
public record Artifact(
        Path path,
        String group,
        String id,
        String version,
        String packaging,
        String classifier,
        String extension) {

    /**
     * Opens an {@link InputStream} to read the artifact's contents.
     * @return the input stream
     */
    public InputStream read() {
        try {
            return Files.newInputStream(path());
        } catch (IOException e) {
            throw new SdkException(e);
        }
    }

}
