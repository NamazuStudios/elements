package dev.getelements.elements.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;
import java.util.Set;
import java.util.UUID;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Regression coverage for #148: a configured-but-unreachable Maven repository must not fail the whole
 * resolution when the artifacts it needs are already present in the local repository. Before the fix,
 * anything that wasn't an {@code ArtifactNotFoundException}/{@code MetadataNotFoundException} produced a
 * hard {@code SdkException}, so a repository that was merely down (connection refused, DNS failure,
 * timeout) bricked the deployment even with a warm local cache.
 * <p>
 * The test fabricates a local Maven repository (via the {@code maven.repo.local} system property) holding
 * an artifact whose {@code _remote.repositories} tracking file attributes it to a repository id that is
 * <em>not</em> configured for the resolution. That forces the online attempt onto the configured
 * repository, which is deliberately unreachable ({@code http://127.0.0.1:1/} refuses connections), exactly
 * mirroring the production failure: the artifact is on disk, but the resolver cannot reach the repository
 * it is recorded against.
 */
public class CachingShrinkwrapElementArtifactLoaderUnreachableRepositoryTest {

    /**
     * The repository id recorded in the fabricated {@code _remote.repositories} tracking file. Deliberately
     * different from the configured repository id, so the enhanced local repository manager considers the
     * cached artifact unavailable for this resolution and attempts the network.
     */
    private static final String RECORDED_REPO_ID = "elsewhere";

    private static final String CACHED_MUST_RESOLVE =
            "a locally cached artifact must resolve even when its repository is unreachable";

    private static final String ARTIFACT = "com.example:demo:1.0";

    private static final String MISSING_ARTIFACT = "com.example:definitely-not-in-any-repository:0.0.1";

    private static final ArtifactRepository UNREACHABLE =
            new ArtifactRepository("unreachable", "http://127.0.0.1:1/");

    private static final Set<ArtifactRepository> UNREACHABLE_ONLY = Set.of(UNREACHABLE);

    private static final String MAVEN_LOCAL_REPO_PROPERTY = "maven.repo.local";

    private final ElementArtifactLoader loader = new CachingShrinkwrapElementArtifactLoader();

    private Path localRepository;

    @BeforeMethod
    public void stageLocalRepository() throws IOException {

        localRepository = Files.createTempDirectory("m2-unreachable-test");

        final var artifactDir = localRepository
                .resolve("com").resolve("example").resolve("demo").resolve("1.0");

        Files.createDirectories(artifactDir);

        Files.writeString(artifactDir.resolve("demo-1.0.pom"), """
                <?xml version="1.0" encoding="UTF-8"?>
                <project xmlns="http://maven.apache.org/POM/4.0.0">
                    <modelVersion>4.0.0</modelVersion>
                    <groupId>com.example</groupId>
                    <artifactId>demo</artifactId>
                    <version>1.0</version>
                </project>
                """, StandardCharsets.UTF_8);

        // Contents are irrelevant; the loader only hands the file to a ClassLoader.
        Files.write(artifactDir.resolve("demo-1.0.jar"), new byte[] {0x50, 0x4b, 0x05, 0x06}); // empty zip

        Files.writeString(artifactDir.resolve("_remote.repositories"),
                "demo-1.0.jar>" + RECORDED_REPO_ID + "=\n"
                        + "demo-1.0.pom>" + RECORDED_REPO_ID + "=\n",
                StandardCharsets.UTF_8);

        // Read per resolution by ShrinkWrap's settings builder, mirroring how
        // applyLocalRepositoryOverride() promotes the platform override in production.
        System.setProperty(MAVEN_LOCAL_REPO_PROPERTY, localRepository.toString());

    }

    @AfterMethod
    public void clearLocalRepository() throws IOException {
        System.clearProperty(MAVEN_LOCAL_REPO_PROPERTY);
        if (localRepository != null) {
            try (var paths = Files.walk(localRepository)) {
                paths.sorted(Comparator.reverseOrder()).forEach(p -> p.toFile().delete());
            }
        }
    }

    @Test
    public void findClassLoaderFallsBackToLocalCacheWhenRepositoryUnreachable() {
        final var result = loader.findClassLoader(null, UNREACHABLE_ONLY, ARTIFACT);
        assertTrue(result.isPresent(), CACHED_MUST_RESOLVE);
    }

    @Test
    public void findArtifactFallsBackToLocalCacheWhenRepositoryUnreachable() {
        final var result = loader.findArtifact(UNREACHABLE_ONLY, ARTIFACT);
        assertTrue(result.isPresent(), CACHED_MUST_RESOLVE);
    }

    @Test
    public void findClasspathForArtifactFallsBackToLocalCacheWhenRepositoryUnreachable() {
        final var result = loader.findClasspathForArtifact(UNREACHABLE_ONLY, ARTIFACT);
        assertFalse(result.toList().isEmpty(), CACHED_MUST_RESOLVE);
    }

    @Test
    public void unreachableRepositoryAndNoCachedCopyFailsNamingRepository() {

        final var ex = expectThrows(
                SdkException.class,
                () -> loader.findArtifact(UNREACHABLE_ONLY, MISSING_ARTIFACT)
        );

        final var message = String.valueOf(ex.getMessage());
        assertTrue(message.contains(UNREACHABLE.id()),
                "failure must name the unreachable repository, was: " + message);
        assertTrue(message.contains(UNREACHABLE.url()),
                "failure must include the unreachable repository URL, was: " + message);
        assertTrue(message.contains(MISSING_ARTIFACT),
                "failure must name the coordinates, was: " + message);

    }

}
