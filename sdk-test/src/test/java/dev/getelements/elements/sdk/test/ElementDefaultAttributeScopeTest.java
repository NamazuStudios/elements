package dev.getelements.elements.sdk.test;

import dev.getelements.elements.config.DefaultConfigurationSupplier;
import dev.getelements.elements.sdk.Attributes;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.MutableElementRegistry;
import dev.getelements.elements.sdk.PermittedTypesClassLoader;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import dev.getelements.elements.sdk.util.TemporaryFiles;

import java.util.Properties;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Factory;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.file.Path;
import java.util.List;

import static dev.getelements.elements.sdk.ElementPathLoader.CLASSPATH_DIR;
import static dev.getelements.elements.sdk.ElementPathLoader.SPI_DIR;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_A;
import static dev.getelements.elements.sdk.test.TestElementArtifact.VARIANT_B;
import static dev.getelements.elements.sdk.test.TestElementSpi.GUICE_7_0_X;
import static dev.getelements.elements.sdk.test.TestUtils.layoutSkeletonElement;
import static dev.getelements.elements.sdk.test.element.TestService.SERVER_OVERRIDABLE_ATTR;
import static dev.getelements.elements.sdk.test.element.TestService.SHARED_DEFAULT_ATTR;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Verifies the {@code @ElementDefaultAttribute} four-tier priority contract (last wins):
 *
 * <ol>
 *   <li><b>SYSTEM_ATTRIBUTES</b> (lowest) — global server floor from env/config files.
 *       Classpath-scanned {@code @ElementDefaultAttribute} values are deliberately excluded
 *       (see {@link dev.getelements.elements.config.DefaultConfigurationSupplier#getExplicitProperties()})
 *       so server-level scan defaults cannot shadow element re-declarations.</li>
 *   <li><b>Element {@code @ElementDefaultAttribute}</b> — element-declared defaults override
 *       SYSTEM_ATTRIBUTES, enabling per-element customisation of shared keys.</li>
 *   <li><b>Global operator attributes</b> ({@code GLOBAL_ELEMENT_ATTRIBUTES}) — operator-set
 *       explicit properties (env vars, config files, {@code withProperties()} in the local SDK)
 *       override element declared defaults.  Applied inside the {@code attributes} layer by
 *       {@code DeploymentContext.createAttributesForPath} so they are still overridable by
 *       per-element path attributes.</li>
 *   <li><b>Per-element path attributes</b> (highest) — deployment-specific values set in the
 *       deployment wizard; always win.</li>
 * </ol>
 *
 * <p>Two flavours of element-default isolation are tested:</p>
 * <ul>
 *   <li><b>Cross-element bleeding</b>: VARIANT_A declares {@code "value-from-a"} and VARIANT_B
 *       declares {@code "value-from-b"} for {@link dev.getelements.elements.sdk.test.element.TestService#SHARED_DEFAULT_ATTR}.
 *       Neither value must bleed into the sibling element via {@code SYSTEM_ATTRIBUTES}.</li>
 *   <li><b>Server-scan-default override</b>: {@link dev.getelements.elements.sdk.test.element.TestService#SERVER_OVERRIDABLE_ATTR}
 *       is declared by a non-element interface (simulating {@code auth.enabled=false} in server
 *       framework code).  VARIANT_A re-declares it with {@code "element-a-override"}, and that
 *       element declaration must win over the server scan default.</li>
 * </ul>
 */
public class ElementDefaultAttributeScopeTest {

    private static final String SYSTEM_ONLY_ATTR = "dev.getelements.elements.sdk.test.element.system.only";

    private static final TestArtifactRegistry testArtifactRegistry = new TestArtifactRegistry();

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(ElementDefaultAttributeScopeTest.class);

    private final Path baseDirectory = temporaryFiles.createTempDirectory();

    private final Path variantADirectory = baseDirectory.resolve("variant_a");

    private final Path variantBDirectory = baseDirectory.resolve("variant_b");

    private final TestElementSpi elementSpi;

    @Factory
    public static Object[] getTestFixtures() {
        return new Object[] { new ElementDefaultAttributeScopeTest(GUICE_7_0_X) };
    }

    public ElementDefaultAttributeScopeTest(final TestElementSpi elementSpi) {
        this.elementSpi = elementSpi;
    }

    @BeforeClass
    public void arrangeElementsInDirectory() throws IOException {
        layoutSkeletonElement(variantADirectory, VARIANT_A.getAttributes());
        layoutSkeletonElement(variantBDirectory, VARIANT_B.getAttributes());

        testArtifactRegistry.copySpiTo(elementSpi, variantADirectory.resolve(SPI_DIR));
        testArtifactRegistry.copySpiTo(elementSpi, variantBDirectory.resolve(SPI_DIR));

        testArtifactRegistry.unpackArtifact(VARIANT_A, variantADirectory.resolve(CLASSPATH_DIR));
        testArtifactRegistry.unpackArtifact(VARIANT_B, variantBDirectory.resolve(CLASSPATH_DIR));
    }

    // -------------------------------------------------------------------------
    // Priority tier 2: element @ElementDefaultAttribute beats SYSTEM_ATTRIBUTES (tier 1)
    // -------------------------------------------------------------------------

    /**
     * Each element resolves its own declared {@code @ElementDefaultAttribute} when SYSTEM_ATTRIBUTES
     * is empty for that key — the baseline isolation case.
     */
    @Test
    public void testEachElementUsesItsOwnDeclaredDefault() {

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-a",
                "VARIANT_A must resolve its own declared @ElementDefaultAttribute"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-b",
                "VARIANT_B must resolve its own declared @ElementDefaultAttribute"
        );

    }

    /**
     * An element's {@code @ElementDefaultAttribute} wins over a conflicting value in
     * {@code SYSTEM_ATTRIBUTES} (e.g. a server-level scan default for {@code auth.enabled=false}
     * while the element declares {@code auth.enabled=true}).
     *
     * <p>This is the core of the priority contract: {@code SYSTEM_ATTRIBUTES} is a global floor;
     * element declared defaults sit above it.  Operators who need to force a specific value for a
     * specific element must use per-element path attributes (the highest tier).</p>
     */
    @Test
    public void testElementDefaultBeatsSystemAttribute() {

        final Attributes systemAttributes = new SimpleAttributes.Builder()
                .setAttribute(SHARED_DEFAULT_ATTR, "system-value")
                .build();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .baseAttributes(systemAttributes)
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-a",
                "VARIANT_A's @ElementDefaultAttribute must beat the SYSTEM_ATTRIBUTES value"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-b",
                "VARIANT_B's @ElementDefaultAttribute must beat the SYSTEM_ATTRIBUTES value"
        );

    }

    /**
     * When an element has <em>no</em> declared default for a key, {@code SYSTEM_ATTRIBUTES} still
     * provides the value — confirming it acts as a global floor rather than being ignored entirely.
     */
    @Test
    public void testSystemAttributeIsUsedWhenElementHasNoDeclaredDefault() {

        final Attributes systemAttributes = new SimpleAttributes.Builder()
                .setAttribute(SYSTEM_ONLY_ATTR, "from-system")
                .build();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .baseAttributes(systemAttributes)
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SYSTEM_ONLY_ATTR),
                "from-system",
                "Key with no element-declared default must fall through to SYSTEM_ATTRIBUTES"
        );

    }

    // -------------------------------------------------------------------------
    // Priority tier 4: per-element path attributes beat everything
    // -------------------------------------------------------------------------

    /**
     * A per-path {@code attributesLoader} value wins above both {@code SYSTEM_ATTRIBUTES} and
     * the element's declared default — it is the authoritative operator override mechanism for
     * a specific element in a specific deployment.
     */
    @Test
    public void testPathAttributeBeatsSystemAttributeAndElementDefault() {

        final var pathOverride = "path-override";

        final Attributes systemAttributes = new SimpleAttributes.Builder()
                .setAttribute(SHARED_DEFAULT_ATTR, "system-value")
                .build();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .baseAttributes(systemAttributes)
                .attributesLoader((diskAttrs, elementPath) -> {
                    if (elementPath.equals(variantADirectory)) {
                        return new SimpleAttributes.Builder()
                                .from(diskAttrs)
                                .setAttribute(SHARED_DEFAULT_ATTR, pathOverride)
                                .build();
                    }
                    return diskAttrs;
                })
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                pathOverride,
                "Per-path attribute must beat both SYSTEM_ATTRIBUTES and VARIANT_A's element default"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-b",
                "VARIANT_B had no path override — must retain its element-declared default"
        );

    }

    // -------------------------------------------------------------------------
    // Priority tier 3: operator explicit attributes beat element defaults
    // -------------------------------------------------------------------------

    /**
     * Simulates the {@code GLOBAL_ELEMENT_ATTRIBUTES} tier: operator-set explicit properties
     * (applied in {@code DeploymentContext.createAttributesForPath} between disk attrs and
     * per-element path attrs) override element {@code @ElementDefaultAttribute} declarations.
     *
     * <p>In production this tier is populated from
     * {@link dev.getelements.elements.config.DefaultConfigurationSupplier#getExplicitProperties()}.
     * In the local SDK it comes from {@code ElementsLocalBuilder.withProperties()}.</p>
     */
    @Test
    public void testOperatorExplicitAttributeBeatsElementDefault() {

        final var operatorValue = "operator-explicit";

        final Attributes operatorAttrs = new SimpleAttributes.Builder()
                .setAttribute(SHARED_DEFAULT_ATTR, operatorValue)
                .build();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                // No baseAttributes — simulates empty SYSTEM_ATTRIBUTES
                // attributesLoader merges global operator attrs then per-element, mirroring
                // DeploymentContext.createAttributesForPath with globalAttributes injected.
                .attributesLoader((diskAttrs, elementPath) -> new SimpleAttributes.Builder()
                        .from(diskAttrs)
                        .from(operatorAttrs)
                        .build()
                )
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                operatorValue,
                "Operator explicit attribute must beat VARIANT_A's @ElementDefaultAttribute"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                operatorValue,
                "Operator explicit attribute must beat VARIANT_B's @ElementDefaultAttribute"
        );

    }

    /**
     * Per-element path attributes (tier 4) beat operator explicit attributes (tier 3).
     * Operator explicit attributes are simulated as applied globally; per-element path
     * attributes override for specific elements.
     */
    @Test
    public void testPathAttributeBeatsOperatorExplicitAttribute() {

        final var operatorValue = "operator-explicit";
        final var pathOverride = "path-override";

        final Attributes operatorAttrs = new SimpleAttributes.Builder()
                .setAttribute(SHARED_DEFAULT_ATTR, operatorValue)
                .build();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .attributesLoader((diskAttrs, elementPath) -> {
                    final var builder = new SimpleAttributes.Builder()
                            .from(diskAttrs)
                            .from(operatorAttrs);
                    if (elementPath.equals(variantADirectory)) {
                        builder.setAttribute(SHARED_DEFAULT_ATTR, pathOverride);
                    }
                    return builder.build();
                })
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                pathOverride,
                "Per-element path attribute must beat operator explicit attribute for VARIANT_A"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                operatorValue,
                "VARIANT_B had no path override — must retain operator explicit attribute value"
        );

    }

    // -------------------------------------------------------------------------
    // Production-path: DefaultConfigurationSupplier and SYSTEM_ATTRIBUTES
    // -------------------------------------------------------------------------

    /**
     * Verifies that {@link DefaultConfigurationSupplier#getExplicitProperties()} excludes
     * classpath-scanned {@code @ElementDefaultAttribute} values so they never reach
     * {@code SYSTEM_ATTRIBUTES} and cannot bleed between elements.
     *
     * <p>In the test environment element JARs are loaded from disk artifacts (isolated classloaders)
     * and are not on the JVM classpath.  Neither {@link dev.getelements.elements.sdk.test.element.TestService#SHARED_DEFAULT_ATTR}
     * nor any other element-only key should appear in explicit properties.</p>
     */
    @Test
    public void testScannedDefaultsAreAbsentFromExplicitProperties() {

        final var supplier = new DefaultConfigurationSupplier(new Properties());

        assertFalse(
                supplier.getExplicitProperties().containsKey(SHARED_DEFAULT_ATTR),
                "Element-only key must be absent from explicit properties — operator has not set it"
        );

        assertFalse(
                supplier.getExplicitProperties().containsKey(SERVER_OVERRIDABLE_ATTR),
                "SERVER_OVERRIDABLE_ATTR must be absent from explicit properties — operator has not set it"
        );

    }

    /**
     * Simulates the production path (case 1 — cross-element bleeding): builds
     * {@code SYSTEM_ATTRIBUTES} from {@link DefaultConfigurationSupplier#getExplicitProperties()}
     * and confirms each element still gets its own declared default for a key the operator never
     * explicitly set.
     */
    @Test
    public void testScannedSystemAttributesDoNotBleedBetweenElements() {

        final var supplier = new DefaultConfigurationSupplier(new Properties());
        final var builder = new SimpleAttributes.Builder();
        supplier.getExplicitProperties().forEach((k, v) -> builder.setAttribute(k.toString(), v.toString()));
        final var baseAttributes = builder.build().immutableCopy();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .baseAttributes(baseAttributes)
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        final var elementB = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_B.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_B not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-a",
                "VARIANT_A must get its own declared default — no cross-element bleed"
        );

        assertEquals(
                elementB.getElementRecord().attributes().getAttribute(SHARED_DEFAULT_ATTR),
                "value-from-b",
                "VARIANT_B must get its own declared default — no cross-element bleed"
        );

    }

    /**
     * Simulates the production path (case 2 — server-scan-default override): {@link dev.getelements.elements.sdk.test.element.TestService#SERVER_OVERRIDABLE_ATTR}
     * is declared in a non-element interface with {@code "server-default"} (visible to the
     * classpath scan), while VARIANT_A re-declares it with {@code "element-a-override"}.
     *
     * <p>Using {@link DefaultConfigurationSupplier#getExplicitProperties()} for {@code SYSTEM_ATTRIBUTES}
     * (as {@link dev.getelements.elements.guice.ConfigurationModule} does in production) means the
     * server scan default never reaches {@code SYSTEM_ATTRIBUTES}.  The element's declared default
     * then wins via the standard priority chain.</p>
     */
    @Test
    public void testServerScanDefaultDoesNotOverrideElementDeclaredDefault() {

        final var supplier = new DefaultConfigurationSupplier(new Properties());

        assertTrue(
                supplier.getDefaultProperties().containsKey(SERVER_OVERRIDABLE_ATTR),
                "SERVER_OVERRIDABLE_ATTR must appear in scanned defaults (TestService is a non-element class)"
        );

        assertFalse(
                supplier.getExplicitProperties().containsKey(SERVER_OVERRIDABLE_ATTR),
                "SERVER_OVERRIDABLE_ATTR must be absent from explicit properties — operator has not set it"
        );

        final var builder = new SimpleAttributes.Builder();
        supplier.getExplicitProperties().forEach((k, v) -> builder.setAttribute(k.toString(), v.toString()));
        final var baseAttributes = builder.build().immutableCopy();

        final var registry = MutableElementRegistry.newDefaultInstance();
        final var loader = ElementPathLoader.newDefaultInstance();
        final var parent = new PermittedTypesClassLoader();

        loader.load(ElementPathLoader.LoadConfiguration.builder()
                .registry(registry)
                .paths(List.of(baseDirectory))
                .parent(parent)
                .baseAttributes(baseAttributes)
                .build()
        ).toList();

        final var elementA = registry.stream()
                .filter(e -> e.getElementRecord().definition().name().equals(VARIANT_A.getElementName()))
                .findFirst()
                .orElseThrow(() -> new AssertionError("VARIANT_A not loaded"));

        assertEquals(
                elementA.getElementRecord().attributes().getAttribute(SERVER_OVERRIDABLE_ATTR),
                "element-a-override",
                "VARIANT_A's @ElementDefaultAttribute must win over the server-level scan default"
        );

    }

}
