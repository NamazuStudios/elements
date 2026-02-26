package dev.getelements.elements.sdk.deployment;

import dev.getelements.elements.sdk.model.system.ElementArtifactRepository;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class TransientDeploymentRequestTest {

    @Test
    public void testBuilderWithElementPath() {
        final var request = TransientDeploymentRequest.builder()
                .elementPath()
                    .path("test/path")
                    .addSpiArtifact("com.example:spi:1.0.0")
                    .addElementArtifact("com.example:element:1.0.0")
                    .attribute("key", "value")
                    .endElementPath()
                .useDefaultRepositories(true)
                .build();

        assertNotNull(request);
        assertNotNull(request.elements());
        assertEquals(request.elements().size(), 1);

        final var element = request.elements().get(0);
        assertEquals(element.path(), "test/path");
        assertNotNull(element.spiArtifacts());
        assertEquals(element.spiArtifacts().size(), 1);
        assertEquals(element.spiArtifacts().get(0), "com.example:spi:1.0.0");
        assertNotNull(element.elementArtifacts());
        assertEquals(element.elementArtifacts().size(), 1);
        assertEquals(element.elementArtifacts().get(0), "com.example:element:1.0.0");
        assertNotNull(element.attributes());
        assertEquals(element.attributes().get("key"), "value");
    }

    @Test
    public void testBuilderWithMultipleElementPaths() {
        final var request = TransientDeploymentRequest.builder()
                .elementPath()
                    .path("path1")
                    .addSpiArtifacts(List.of("com.example:spi1:1.0.0", "com.example:spi2:1.0.0"))
                    .addElementArtifacts(List.of("com.example:element1:1.0.0"))
                    .endElementPath()
                .elementPath()
                    .path("path2")
                    .addSpiArtifact("com.example:spi3:1.0.0")
                    .addElementArtifact("com.example:element2:1.0.0")
                    .attribute("enabled", true)
                    .endElementPath()
                .useDefaultRepositories(false)
                .build();

        assertNotNull(request);
        assertNotNull(request.elements());
        assertEquals(request.elements().size(), 2);

        final var element1 = request.elements().get(0);
        assertEquals(element1.path(), "path1");
        assertEquals(element1.spiArtifacts().size(), 2);

        final var element2 = request.elements().get(1);
        assertEquals(element2.path(), "path2");
        assertEquals(element2.spiArtifacts().size(), 1);
        assertNotNull(element2.attributes());
        assertEquals(element2.attributes().get("enabled"), true);
    }

    @Test
    public void testBuilderWithElementPackage() {
        final var request = TransientDeploymentRequest.builder()
                .elementPackage()
                    .elmArtifact("com.example:elm:1.0.0")
                    .addPathSpiClassPath("element1", "com.example:spi1:1.0.0")
                    .pathAttribute("element1", "config", "value1")
                    .pathAttribute("element2", "enabled", true)
                    .endElementPackage()
                .useDefaultRepositories(true)
                .build();

        assertNotNull(request);
        assertNotNull(request.packages());
        assertEquals(request.packages().size(), 1);

        final var pkg = request.packages().get(0);
        assertEquals(pkg.elmArtifact(), "com.example:elm:1.0.0");
        assertNotNull(pkg.pathSpiClassPaths());
        assertTrue(pkg.pathSpiClassPaths().containsKey("element1"));
        assertNotNull(pkg.pathAttributes());
        assertTrue(pkg.pathAttributes().containsKey("element1"));
        assertEquals(pkg.pathAttributes().get("element1").get("config"), "value1");
        assertTrue(pkg.pathAttributes().containsKey("element2"));
        assertEquals(pkg.pathAttributes().get("element2").get("enabled"), true);
    }

    @Test
    public void testBuilderWithMixedElementsAndPackages() {
        final var request = TransientDeploymentRequest.builder()
                .elementPath()
                    .path("standalone")
                    .addSpiArtifact("com.example:spi:1.0.0")
                    .addElementArtifact("com.example:element:1.0.0")
                    .endElementPath()
                .elementPackage()
                    .elmArtifact("com.example:package:1.0.0")
                    .pathAttribute("pkg-element", "key", "value")
                    .endElementPackage()
                .addRepository(new ElementArtifactRepository("central", "https://repo.maven.apache.org/maven2"))
                .useDefaultRepositories(true)
                .build();

        assertNotNull(request);
        assertNotNull(request.elements());
        assertEquals(request.elements().size(), 1);
        assertNotNull(request.packages());
        assertEquals(request.packages().size(), 1);
        assertNotNull(request.repositories());
        assertEquals(request.repositories().size(), 1);
        assertTrue(request.useDefaultRepositories());
    }

    @Test
    public void testBuilderWithPathAttributes() {
        final var request = TransientDeploymentRequest.builder()
                .addPathAttribute("element1", "key1", "value1")
                .addPathAttribute("element1", "key2", "value2")
                .addPathAttribute("element2", "key3", "value3")
                .elementPath()
                    .path("element1")
                    .addElementArtifact("com.example:element:1.0.0")
                    .endElementPath()
                .build();

        assertNotNull(request);
        assertNotNull(request.pathAttributes());
        assertTrue(request.pathAttributes().containsKey("element1"));
        assertEquals(request.pathAttributes().get("element1").get("key1"), "value1");
        assertEquals(request.pathAttributes().get("element1").get("key2"), "value2");
        assertTrue(request.pathAttributes().containsKey("element2"));
        assertEquals(request.pathAttributes().get("element2").get("key3"), "value3");
    }

    @Test
    public void testBuilderWithPathSpiClasspath() {
        final var request = TransientDeploymentRequest.builder()
                .addPathSpiClasspath("element1", List.of("com.example:spi1:1.0.0", "com.example:spi2:1.0.0"))
                .addPathSpiClasspath("element2", List.of("com.example:spi3:1.0.0"))
                .elementPath()
                    .path("element1")
                    .addElementArtifact("com.example:element:1.0.0")
                    .endElementPath()
                .build();

        assertNotNull(request);
        assertNotNull(request.pathSpiClasspath());
        assertTrue(request.pathSpiClasspath().containsKey("element1"));
        assertEquals(request.pathSpiClasspath().get("element1").size(), 2);
        assertTrue(request.pathSpiClasspath().containsKey("element2"));
        assertEquals(request.pathSpiClasspath().get("element2").size(), 1);
    }

    @Test
    public void testBuilderCreatesValidRequest() {
        final var request = TransientDeploymentRequest.builder()
                .elementPath()
                    .path("test")
                    .addSpiArtifact("com.example:artifact:1.0.0")
                    .attribute("key", "value")
                    .endElementPath()
                .build();

        // Verify request structure
        assertNotNull(request.elements());
        assertEquals(request.elements().size(), 1);
        assertEquals(request.elements().get(0).path(), "test");
    }

    @Test
    public void testBuilderCreatesPackages() {
        final var request = TransientDeploymentRequest.builder()
                .elementPackage()
                    .elmArtifact("com.example:elm:1.0.0")
                    .pathAttribute("element1", "key", "value")
                    .endElementPackage()
                .build();

        assertNotNull(request.packages());
        assertEquals(request.packages().size(), 1);
        final var pkg = request.packages().get(0);
        assertEquals(pkg.elmArtifact(), "com.example:elm:1.0.0");
        assertNotNull(pkg.pathAttributes());
        assertEquals(pkg.pathAttributes().get("element1").get("key"), "value");
    }
}
