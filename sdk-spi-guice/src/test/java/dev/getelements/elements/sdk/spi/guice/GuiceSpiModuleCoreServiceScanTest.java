package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Guice;
import dev.getelements.elements.sdk.service.application.MatchmakingApplicationConfigurationService;
import dev.getelements.elements.sdk.spi.DefaultElementLoaderFactory;
import dev.getelements.elements.sdk.spi.RootElementRegistry;
import dev.getelements.elements.sdk.spi.guice.fixture.broadscan.BroadScanTestService;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.util.Map;

import static java.util.stream.Collectors.toSet;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;
import static org.testng.Assert.assertTrue;

/**
 * Regression test for issue #32: an Element whose {@code additionalPackages} reach into
 * {@code dev.getelements.elements.sdk.service.application} must not absorb the platform's core
 * service exports into its own {@link dev.getelements.elements.sdk.record.ElementRecord}. When they
 * leaked in, they carried no implementation, so {@link GuiceSpiModule} exposed them without binding
 * them and injector creation failed with {@code [Guice/ExposedButNotBound]} -- most visibly for
 * {@link MatchmakingApplicationConfigurationService}.
 *
 * <p>The platform's own packages are declared {@code @ElementDefinition} (e.g. the root of
 * {@code dev.getelements.elements.sdk.service}), so the element scan must reject them as sibling
 * element definitions regardless of any additionalPackages that point into them.</p>
 */
public class GuiceSpiModuleCoreServiceScanTest {

    private final DefaultElementLoaderFactory factory = new DefaultElementLoaderFactory();

    @Test
    public void broadAdditionalPackagesIntoCoreServiceApplicationDoNotLeakIntoTheElementRecord() {

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                BroadScanTestService.class.getPackage()
        );

        final var exposed = elementRecord
                .services()
                .stream()
                .flatMap(esr -> esr.export().exposed().stream())
                .collect(toSet());

        assertTrue(exposed.contains(BroadScanTestService.class), "the element's own service should be exported");

        assertFalse(
                exposed.contains(MatchmakingApplicationConfigurationService.class),
                "core platform export leaked into the element record: " + exposed
        );

        assertFalse(
                exposed.stream().anyMatch(t -> t.getName().startsWith("dev.getelements.elements.sdk.service.")),
                "core platform exports leaked into the element record: " + exposed
        );

    }

    @Test
    public void elementLoadsCleanlyWhenAdditionalPackagesPointIntoCoreServiceApplication() {

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                BroadScanTestService.class.getPackage()
        );

        final var injector = Guice.createInjector(new GuiceSpiModule(new RootElementRegistry(), elementRecord));

        assertEquals(injector.getInstance(BroadScanTestService.class).get(), "broad-scan");

    }

}
