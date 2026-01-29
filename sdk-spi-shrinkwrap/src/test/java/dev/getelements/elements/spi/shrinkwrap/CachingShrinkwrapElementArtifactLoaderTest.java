package dev.getelements.elements.spi.shrinkwrap;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.record.ArtifactCoordinates;
import dev.getelements.elements.sdk.spi.shrinkwrap.CachingShrinkwrapElementArtifactLoader;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ServiceLoader;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

public class CachingShrinkwrapElementArtifactLoaderTest {

    /**
     * Common test artifacts
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

    private final Set<String> DELIBERATELY_MISSING_ARTIFACTS = Set.of(
            "com.example:does-not-exist:0.0.1"
    );

    private final ElementArtifactLoader loader = new CachingShrinkwrapElementArtifactLoader();

    @DataProvider
    public Object[][] getTestArtifacts() {
        return TEST_ARTIFACTS
                .stream()
                .map(ArtifactCoordinates::fromCoordinatesAndDefaultRepository)
                .map(a -> new Object[] {a})
                .toArray(Object[][]::new);
    }

    @DataProvider
    public Object[][] getMissingTestArtifacts() {
        return DELIBERATELY_MISSING_ARTIFACTS
                .stream()
                .map(ArtifactCoordinates::fromCoordinatesAndDefaultRepository)
                .map(a -> new Object[] {a})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getTestArtifacts")
    public void testGetArtifacts(final ArtifactCoordinates coordinates) {
        final var result = loader.tryGetClassLoader(null, Set.of(coordinates));
        assertTrue(result.isPresent());
    }

    @Test(dataProvider = "getMissingTestArtifacts")
    public void testMissingArtifacts(final ArtifactCoordinates coordinates) {
        final var result = loader.tryGetClassLoader(null, Set.of(coordinates));
        assertFalse(result.isPresent());
    }

    @Test
    public void testGetAll() {

        final var all = TEST_ARTIFACTS
                .stream()
                .map(ArtifactCoordinates::fromCoordinatesAndDefaultRepository)
                .collect(Collectors.toUnmodifiableSet());

        final var result = loader.tryGetClassLoader(null, all);
        assertTrue(result.isPresent());

    }
}
