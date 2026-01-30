package dev.getelements.elements.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.record.ArtifactRepository;
import dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.Set;

import static java.util.stream.Collectors.toUnmodifiableSet;
import static org.testng.Assert.assertFalse;
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

    private final ElementArtifactLoader loader = new CachingShrinkwrapElementArtifactLoader();

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
        final var result = loader.tryGetClassLoader(null, ArtifactRepository.DEFAULTS, coordinates);
        assertTrue(result.isPresent());
    }

    @Test(dataProvider = "getMissingTestArtifacts")
    public void testMissingArtifacts(final String coordinates) {
        final var result = loader.tryGetClassLoader(null, ArtifactRepository.DEFAULTS, coordinates);
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAll() {

        final var all = TEST_ARTIFACTS
                .stream()
                .collect(toUnmodifiableSet());

        final var result = loader.tryGetClassLoader(null, ArtifactRepository.DEFAULTS, all);
        assertTrue(result.isPresent());

    }

}
