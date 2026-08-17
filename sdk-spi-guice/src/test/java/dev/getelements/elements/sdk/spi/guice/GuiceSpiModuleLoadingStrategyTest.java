package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Guice;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.spi.DefaultElementLoaderFactory;
import dev.getelements.elements.sdk.spi.RootElementRegistry;
import dev.getelements.elements.sdk.spi.guice.fixture.guicemoduleonly.SharedTestService;
import dev.getelements.elements.sdk.spi.guice.fixture.legacy.LegacyTestService;
import dev.getelements.elements.sdk.spi.guice.fixture.strict.UnboundTestService;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Proves both {@code GuiceOptions.LoadingStrategy} code paths through {@link GuiceSpiModule}: the default
 * {@code LEGACY} behavior is unchanged (issue #32 must not regress existing Elements), and the opt-in
 * {@code GUICE_MODULE_ONLY} strategy lets a third-party Element author defer entirely to their own
 * {@code @GuiceElementModule}, avoiding the exact {@code [Guice/ExposedButNotBound]} crash reported in #32.
 */
public class GuiceSpiModuleLoadingStrategyTest {

    private final DefaultElementLoaderFactory factory = new DefaultElementLoaderFactory();

    @Test
    public void legacyStrategyBindsAndExposesAnnotationDerivedImplementation() {

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                LegacyTestService.class.getPackage()
        );

        final var injector = Guice.createInjector(new GuiceSpiModule(new RootElementRegistry(), elementRecord));
        final var service = injector.getInstance(LegacyTestService.class);

        assertEquals(service.get(), "legacy");

    }

    @Test
    public void guiceModuleOnlyStrategyDefersEntirelyToTheInstalledModule() {

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                SharedTestService.class.getPackage()
        );

        final var injector = Guice.createInjector(new GuiceSpiModule(new RootElementRegistry(), elementRecord));
        final var service = injector.getInstance(SharedTestService.class);

        // If the annotation-derived AnnotationDerivedImpl binding had not been skipped, Guice would have thrown
        // a duplicate-binding CreationException before this point (two bind() calls for the same key).
        assertEquals(service.get(), "guice-module-derived");

    }

    @Test
    public void strictModeRaisesAClearErrorForAnUnboundExportedService() {

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                UnboundTestService.class.getPackage()
        );

        // Expecting SdkException (thrown by GuiceSpiModule's own strict-mode validation, during configure())
        // rather than Guice's generic CreationException (thrown later, at injector-creation time) proves the
        // new validation actually ran and pre-empted the default failure mode.
        final var exception = expectThrows(SdkException.class, () ->
                Guice.createInjector(new GuiceSpiModule(new RootElementRegistry(), elementRecord)));

        assertTrue(exception.getMessage().contains("UnboundTestService"));

    }

}
