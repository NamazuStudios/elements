package dev.getelements.elements.deployment.jetty.loader;

import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.Element;
import dev.getelements.elements.sdk.record.ElementDefinitionRecord;
import dev.getelements.elements.sdk.record.ElementRecord;
import dev.getelements.elements.servlet.HttpContextRoot;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static dev.getelements.elements.sdk.deployment.ElementContainerService.APPLICATION_PREFIX;
import static dev.getelements.elements.sdk.deployment.ElementContainerService.RS_ROOT;
import static dev.getelements.elements.sdk.deployment.ElementContainerService.WS_ROOT;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ElementPathResolverTest {

    private ElementPathResolver resolver;
    private Element element;
    private Attributes attributes;
    private HttpContextRoot httpContextRoot;
    private List<String> deploymentWarnings;
    private Loader.PendingDeployment pending;

    @BeforeMethod
    public void setUp() {

        resolver = new ElementPathResolver();

        attributes = mock(Attributes.class);

        final var definition = mock(ElementDefinitionRecord.class);
        when(definition.name()).thenReturn("my.element");

        final var record = mock(ElementRecord.class);
        when(record.attributes()).thenReturn(attributes);
        when(record.definition()).thenReturn(definition);

        element = mock(Element.class);
        when(element.getElementRecord()).thenReturn(record);

        httpContextRoot = new HttpContextRoot();
        httpContextRoot.setHttpPathPrefix("/");

        deploymentWarnings = new ArrayList<>();

        pending = new Loader.PendingDeployment(
                uri -> true,
                msg -> {},
                deploymentWarnings::add,
                err -> {},
                e -> {}
        );

        // Default: no attributes present
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.empty());
        when(attributes.getAttributeOptional(RS_ROOT)).thenReturn(Optional.empty());
        when(attributes.getAttributeOptional(WS_ROOT)).thenReturn(Optional.empty());

    }

    // --- (a) Legacy prefix only ---

    @Test
    public void legacyPrefixOnlyResolvesRsPath() {
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("myapp"));
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/rest/myapp");
    }

    @Test
    public void legacyPrefixOnlyResolvesWsPath() {
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("myapp"));
        final var path = resolver.resolveWsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/ws/myapp");
    }

    @Test
    public void legacyPrefixEmitsDeprecationWarning() {
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("myapp"));
        resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertTrue(deploymentWarnings.stream().anyMatch(w -> w.contains("DEPRECATION")),
                "Expected a deprecation warning in the deployment log");
    }

    // --- (b) New attributes only ---

    @Test
    public void newAttributesOnlyResolvesRsPath() {
        when(attributes.getAttributeOptional(RS_ROOT)).thenReturn(Optional.of("/my/api"));
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/my/api");
    }

    @Test
    public void newAttributesOnlyResolvesWsPath() {
        when(attributes.getAttributeOptional(WS_ROOT)).thenReturn(Optional.of("/my/ws"));
        final var path = resolver.resolveWsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/my/ws");
    }

    @Test
    public void newAttributesDoNotEmitDeprecationWarning() {
        when(attributes.getAttributeOptional(RS_ROOT)).thenReturn(Optional.of("/my/api"));
        resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertTrue(deploymentWarnings.isEmpty(), "Expected no deprecation warnings");
    }

    // --- (c) Both present — legacy wins ---

    @Test
    public void legacyPrefixWinsOverRsRoot() {
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("legacy"));
        when(attributes.getAttributeOptional(RS_ROOT)).thenReturn(Optional.of("/should/be/ignored"));
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/rest/legacy");
    }

    @Test
    public void legacyPrefixWinsOverWsRoot() {
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("legacy"));
        when(attributes.getAttributeOptional(WS_ROOT)).thenReturn(Optional.of("/should/be/ignored"));
        final var path = resolver.resolveWsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/ws/legacy");
    }

    // --- Default fallback (no attributes at all) ---

    @Test
    public void defaultFallbackUsesElementNameForRs() {
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/rest/my.element");
    }

    @Test
    public void defaultFallbackUsesElementNameForWs() {
        final var path = resolver.resolveWsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/app/ws/my.element");
    }

    // --- HTTP path prefix is respected ---

    @Test
    public void httpPathPrefixIsAppliedToLegacyPath() {
        httpContextRoot.setHttpPathPrefix("/elements");
        when(attributes.getAttributeOptional(APPLICATION_PREFIX)).thenReturn(Optional.of("myapp"));
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/elements/app/rest/myapp");
    }

    @Test
    public void httpPathPrefixIsAppliedToNewRsRoot() {
        httpContextRoot.setHttpPathPrefix("/elements");
        when(attributes.getAttributeOptional(RS_ROOT)).thenReturn(Optional.of("/my/api"));
        final var path = resolver.resolveRsContextPath(element, httpContextRoot, pending);
        assertEquals(path, "/elements/my/api");
    }

}
