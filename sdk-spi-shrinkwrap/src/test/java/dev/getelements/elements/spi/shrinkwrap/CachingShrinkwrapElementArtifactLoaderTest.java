package dev.getelements.elements.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader;
import org.testng.annotations.AfterMethod;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Set;

import static dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader.LOCAL_REPOSITORY_PROPERTY;
import static dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader.applyLocalRepositoryOverride;
import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertNull;
import static org.testng.Assert.assertTrue;

public class CachingShrinkwrapElementArtifactLoaderTest {

    /**
     * Common test artifacts.
     */
    private final Set<String> TEST_ARTIFACTS = Set.of(
            "junit:junit:4.13.2",
            "org.junit.jupiter:junit-jupiter-api:5.10.2",
            "org.junit.jupiter:junit-jupiter-engine:5.10.2",
            "org.hamcrest:hamcrest:2.2",
            "org.assertj:assertj-core:3.25.3",
            "org.mockito:mockito-core:5.10.0",
            "com.google.guava:guava:33.0.0-jre"
    );

    /**
     * Deliberately missing artifacts.
     */
    private final Set<String> DELIBERATELY_MISSING_ARTIFACTS = Set.of(
            "com.example:does-not-exist:0.0.1"
    );

    private static final String MAVEN_LOCAL_REPO_PROPERTY = "maven.repo.local";

    /**
     * An existing-but-empty directory, served as a {@code file://} Maven repository. Resolving a
     * deliberately missing artifact against it fails deterministically with ArtifactNotFoundException
     * (the loader's "not found" path) without contacting any network repository.
     */
    private static Path emptyRepositoryDirectory;

    private final ElementArtifactLoader loader = new CachingShrinkwrapElementArtifactLoader();

    @BeforeClass
    public void createEmptyRepository() throws IOException {
        emptyRepositoryDirectory = Files.createTempDirectory("empty-maven-repo-");
    }

    @AfterMethod
    public void clearProperties() {
        System.clearProperty(LOCAL_REPOSITORY_PROPERTY);
        System.clearProperty(MAVEN_LOCAL_REPO_PROPERTY);
    }

    @DataProvider
    public Object[][] getTestArtifacts() {
        return TEST_ARTIFACTS
                .stream()
                .map(a -> new Object[] {a})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getMissingTestArtifacts() {
        return DELIBERATELY_MISSING_ARTIFACTS
                .stream()
                .map(a -> new Object[] {a})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getTestArtifacts")
    public void testGetArtifacts(final String coordinates) {
        final var result = loader.findClassLoader(null, ArtifactRepository.DEFAULTS, coordinates);
        assertTrue(result.isPresent());
    }

    @Test(dataProvider = "getMissingTestArtifacts")
    public void testMissingArtifacts(final String coordinates) {
        // Hermetic: resolve against a local file:// repository that never contains the artifact.
        //
        // Resolving against ArtifactRepository.DEFAULTS made this test depend on live Maven Central
        // answering 404 for the missing artifact. On 2026-11-24 Central answered 429 (Too Many
        // Requests) instead; the loader correctly classified that as "repository unreachable" and
        // threw SdkException (after the local-cache fallback found nothing), failing the test and
        // main's Branch Build. The unreachable-repository behavior is already covered separately
        // (the loader's offline fallback); this test's job is the not-found path, which a local
        // empty repository exercises deterministically.
        final var hermeticRepositories = Set.of(new ArtifactRepository(
                "empty-local",
                emptyRepositoryDirectory.toUri().toString()));
        final var result = loader.findClassLoader(null, hermeticRepositories, coordinates);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAll() {

        final var all = TEST_ARTIFACTS
                .stream()
                .collect(toUnmodifiableSet());

        final var result = loader.findClassLoader(null, ArtifactRepository.DEFAULTS, all);
        assertTrue(result.isPresent());

    }

    @Test
    public void testLocalRepositoryOverridePromotesConfiguredProperty() {
        System.setProperty(LOCAL_REPOSITORY_PROPERTY, "/tmp/custom-m2-repo");
        applyLocalRepositoryOverride();
        assertEquals(System.getProperty(MAVEN_LOCAL_REPO_PROPERTY), "/tmp/custom-m2-repo");
    }

    @Test
    public void testLocalRepositoryOverrideNoOpWhenNeitherSet() {
        applyLocalRepositoryOverride();
        assertNull(System.getProperty(MAVEN_LOCAL_REPO_PROPERTY));
    }

    @Test
    public void testLocalRepositoryOverrideDoesNotClobberExplicitShrinkwrapProperty() {
        System.setProperty(MAVEN_LOCAL_REPO_PROPERTY, "/tmp/operator-set-repo");
        System.setProperty(LOCAL_REPOSITORY_PROPERTY, "/tmp/custom-m2-repo");
        applyLocalRepositoryOverride();
        assertEquals(System.getProperty(MAVEN_LOCAL_REPO_PROPERTY), "/tmp/operator-set-repo");
    }

}
