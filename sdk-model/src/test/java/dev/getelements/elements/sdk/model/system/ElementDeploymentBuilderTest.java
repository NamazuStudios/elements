package dev.getelements.elements.sdk.model.system;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectState;
import org.testng.annotations.Test;

import java.util.List;
import java.util.Map;

import static org.testng.Assert.*;

public class ElementDeploymentBuilderTest {

    @Test
    public void shouldBuildBasicDeployment() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-123")
                .useDefaultRepositories(true)
                .state(ElementDeploymentState.ENABLED)
                .version(1L)
                .build();

        assertNotNull(deployment);
        assertEquals(deployment.id(), "test-123");
        assertTrue(deployment.useDefaultRepositories());
        assertEquals(deployment.state(), ElementDeploymentState.ENABLED);
        assertEquals(deployment.version(), 1L);
        assertNull(deployment.elements());
        assertNull(deployment.packages());
        assertNull(deployment.repositories());
    }

    @Test
    public void shouldBuildDeploymentWithApplication() {
        final Application app = new Application();
        app.setId("app-id");
        app.setName("test-app");
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-456")
                .application(app)
                .build();

        assertNotNull(deployment);
        assertEquals(deployment.application(), app);
    }

    @Test
    public void shouldBuildDeploymentWithElm() {
        final LargeObjectReference elmRef = new LargeObjectReference();
        elmRef.setId("elm-id");
        elmRef.setUrl("test.elm");
        elmRef.setState(LargeObjectState.UPLOADED);
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-789")
                .elm(elmRef)
                .build();

        assertNotNull(deployment);
        assertEquals(deployment.elm(), elmRef);
    }

    @Test
    public void shouldBuildDeploymentWithElementPath() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-path")
                .elementPath()
                    .path("my-element")
                    .addApiArtifact("com.example:api:1.0")
                    .addSpiArtifact("com.example:spi:1.0")
                    .addElementArtifact("com.example:element:1.0")
                    .attribute("key1", "value1")
                    .endElementPath()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 1);

        final ElementPathDefinition element = deployment.elements().get(0);
        assertEquals(element.path(), "my-element");
        assertNotNull(element.apiArtifacts());
        assertEquals(element.apiArtifacts().size(), 1);
        assertEquals(element.apiArtifacts().get(0), "com.example:api:1.0");
        assertNotNull(element.spiArtifacts());
        assertEquals(element.spiArtifacts().size(), 1);
        assertEquals(element.spiArtifacts().get(0), "com.example:spi:1.0");
        assertNotNull(element.elementArtifacts());
        assertEquals(element.elementArtifacts().size(), 1);
        assertEquals(element.elementArtifacts().get(0), "com.example:element:1.0");
        assertNotNull(element.attributes());
        assertEquals(element.attributes().get("key1"), "value1");
    }

    @Test
    public void shouldBuildDeploymentWithMultipleElementPaths() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-multi-path")
                .elementPath()
                    .path("element-a")
                    .addApiArtifact("com.example:api-a:1.0")
                    .endElementPath()
                .elementPath()
                    .path("element-b")
                    .addApiArtifact("com.example:api-b:1.0")
                    .endElementPath()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 2);
        assertEquals(deployment.elements().get(0).path(), "element-a");
        assertEquals(deployment.elements().get(1).path(), "element-b");
    }

    @Test
    public void shouldBuildDeploymentWithElementPackage() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-package")
                .elementPackage()
                    .elmArtifact("com.example:package:1.0")
                    .pathAttribute("element-a", "config", "production")
                    .pathAttribute("element-a", "timeout", 5000)
                    .pathSpiClassPath("element-b", List.of("com.example:spi:1.0"))
                    .endElementPackage()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 1);

        final ElementPackageDefinition pkg = deployment.packages().get(0);
        assertEquals(pkg.elmArtifact(), "com.example:package:1.0");
        assertNotNull(pkg.pathAttributes());
        assertEquals(pkg.pathAttributes().get("element-a").get("config"), "production");
        assertEquals(pkg.pathAttributes().get("element-a").get("timeout"), 5000);
        assertNotNull(pkg.pathSpiClassPaths());
        assertEquals(pkg.pathSpiClassPaths().get("element-b").get(0), "com.example:spi:1.0");
    }

    @Test
    public void shouldBuildDeploymentWithMultiplePackages() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-multi-package")
                .elementPackage()
                    .elmArtifact("com.example:package-a:1.0")
                    .endElementPackage()
                .elementPackage()
                    .elmArtifact("com.example:package-b:1.0")
                    .endElementPackage()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 2);
        assertEquals(deployment.packages().get(0).elmArtifact(), "com.example:package-a:1.0");
        assertEquals(deployment.packages().get(1).elmArtifact(), "com.example:package-b:1.0");
    }

    @Test
    public void shouldBuildComplexDeployment() {
        final Application app = new Application();
        app.setId("app-id");
        app.setName("test-app");
        final ElementArtifactRepository repo = new ElementArtifactRepository(
                "custom-repo",
                "https://repo.example.com/maven"
        );

        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-complex")
                .application(app)
                .useDefaultRepositories(true)
                .addRepository(repo)
                .state(ElementDeploymentState.ENABLED)
                .version(5L)
                .elementPath()
                    .path("element-one")
                    .addApiArtifact("com.example:api-one:1.0")
                    .addSpiArtifact("com.example:spi-one:1.0")
                    .attribute("enabled", true)
                    .endElementPath()
                .elementPath()
                    .path("element-two")
                    .addApiArtifact("com.example:api-two:2.0")
                    .endElementPath()
                .elementPackage()
                    .elmArtifact("com.example:package:1.0")
                    .pathAttribute("element-a", "mode", "live")
                    .pathAttribute("element-b", "retries", 3)
                    .pathSpiClassPath("element-a", List.of("com.example:custom-spi:1.0"))
                    .endElementPackage()
                .addPathAttribute("global-element", "globalKey", "globalValue")
                .addPathSpiClassPath("global-element", List.of("com.example:global-spi:1.0"))
                .build();

        assertNotNull(deployment);
        assertEquals(deployment.id(), "test-complex");
        assertEquals(deployment.application(), app);
        assertTrue(deployment.useDefaultRepositories());
        assertEquals(deployment.state(), ElementDeploymentState.ENABLED);
        assertEquals(deployment.version(), 5L);

        assertNotNull(deployment.repositories());
        assertEquals(deployment.repositories().size(), 1);
        assertEquals(deployment.repositories().get(0).id(), "custom-repo");

        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 2);

        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 1);

        assertNotNull(deployment.pathAttributes());
        assertEquals(deployment.pathAttributes().get("global-element").get("globalKey"), "globalValue");

        assertNotNull(deployment.pathSpiClassPaths());
        assertEquals(deployment.pathSpiClassPaths().get("global-element").get(0), "com.example:global-spi:1.0");
    }

    @Test
    public void shouldBuildStandaloneElementPathDefinition() {
        final ElementPathDefinition element = ElementDeploymentBuilder.ElementPathDefinitionBuilder.builder()
                .path("standalone-element")
                .addApiArtifact("com.example:api:1.0")
                .addSpiArtifact("com.example:spi:1.0")
                .attribute("standalone", true)
                .build();

        assertNotNull(element);
        assertEquals(element.path(), "standalone-element");
        assertNotNull(element.apiArtifacts());
        assertEquals(element.apiArtifacts().size(), 1);
        assertNotNull(element.spiArtifacts());
        assertEquals(element.spiArtifacts().size(), 1);
        assertNotNull(element.attributes());
        assertEquals(element.attributes().get("standalone"), true);
    }

    @Test
    public void shouldBuildStandaloneElementPackageDefinition() {
        final ElementPackageDefinition pkg = ElementDeploymentBuilder.ElementPackageDefinitionBuilder.builder()
                .elmArtifact("com.example:standalone-package:1.0")
                .pathAttribute("element-x", "key", "value")
                .pathSpiClassPath("element-y", List.of("com.example:spi:1.0"))
                .build();

        assertNotNull(pkg);
        assertEquals(pkg.elmArtifact(), "com.example:standalone-package:1.0");
        assertNotNull(pkg.pathAttributes());
        assertEquals(pkg.pathAttributes().get("element-x").get("key"), "value");
        assertNotNull(pkg.pathSpiClassPaths());
        assertEquals(pkg.pathSpiClassPaths().get("element-y").get(0), "com.example:spi:1.0");
    }

    @Test
    public void shouldHandleEmptyCollectionsAsNull() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-empty")
                .build();

        assertNotNull(deployment);
        assertNull(deployment.elements());
        assertNull(deployment.packages());
        assertNull(deployment.repositories());
        assertNull(deployment.pathAttributes());
        assertNull(deployment.pathSpiClassPaths());
    }

    @Test
    public void shouldBuildElementPathWithListSetters() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-list-setters")
                .elementPath()
                    .path("element-list")
                    .apiArtifacts(List.of("com.example:api1:1.0", "com.example:api2:1.0"))
                    .spiArtifacts(List.of("com.example:spi1:1.0"))
                    .elementArtifacts(List.of("com.example:element1:1.0"))
                    .attributes(Map.of("key1", "value1", "key2", "value2"))
                    .endElementPath()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 1);

        final ElementPathDefinition element = deployment.elements().get(0);
        assertNotNull(element.apiArtifacts());
        assertEquals(element.apiArtifacts().size(), 2);
        assertNotNull(element.spiArtifacts());
        assertEquals(element.spiArtifacts().size(), 1);
        assertNotNull(element.elementArtifacts());
        assertEquals(element.elementArtifacts().size(), 1);
        assertNotNull(element.attributes());
        assertEquals(element.attributes().size(), 2);
    }

    @Test
    public void shouldBuildElementPackageWithMapSetters() {
        final Map<String, List<String>> spiClassPaths = Map.of(
                "element-a", List.of("com.example:spi-a:1.0"),
                "element-b", List.of("com.example:spi-b:1.0")
        );

        final Map<String, Map<String, Object>> attributes = Map.of(
                "element-a", Map.of("key1", "value1"),
                "element-b", Map.of("key2", "value2")
        );

        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-map-setters")
                .elementPackage()
                    .elmArtifact("com.example:package:1.0")
                    .pathSpiClassPaths(spiClassPaths)
                    .pathAttributes(attributes)
                    .endElementPackage()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 1);

        final ElementPackageDefinition pkg = deployment.packages().get(0);
        assertNotNull(pkg.pathSpiClassPaths());
        assertEquals(pkg.pathSpiClassPaths().size(), 2);
        assertNotNull(pkg.pathAttributes());
        assertEquals(pkg.pathAttributes().size(), 2);
    }

    @Test
    public void shouldAddElementDirectly() {
        final ElementPathDefinition element = new ElementPathDefinition(
                "direct-element",
                List.of("com.example:api:1.0"),
                null,
                null,
                null
        );

        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-direct")
                .addElement(element)
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.elements());
        assertEquals(deployment.elements().size(), 1);
        assertEquals(deployment.elements().get(0), element);
    }

    @Test
    public void shouldAddPackageDirectly() {
        final ElementPackageDefinition pkg = new ElementPackageDefinition(
                "com.example:package:1.0",
                null,
                null
        );

        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-direct-package")
                .addPackage(pkg)
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 1);
        assertEquals(deployment.packages().get(0), pkg);
    }

    @Test
    public void shouldSupportAddPathSpiClassPathInPackageBuilder() {
        final ElementDeployment deployment = ElementDeploymentBuilder.builder()
                .id("test-add-spi")
                .elementPackage()
                    .elmArtifact("com.example:package:1.0")
                    .addPathSpiClassPath("element-a", "com.example:spi1:1.0")
                    .addPathSpiClassPath("element-a", "com.example:spi2:1.0")
                    .addPathSpiClassPath("element-b", "com.example:spi3:1.0")
                    .endElementPackage()
                .build();

        assertNotNull(deployment);
        assertNotNull(deployment.packages());
        assertEquals(deployment.packages().size(), 1);

        final ElementPackageDefinition pkg = deployment.packages().get(0);
        assertNotNull(pkg.pathSpiClassPaths());
        assertEquals(pkg.pathSpiClassPaths().get("element-a").size(), 2);
        assertEquals(pkg.pathSpiClassPaths().get("element-b").size(), 1);
    }
}
