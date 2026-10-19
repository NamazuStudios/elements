package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Named;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.guice.GuiceServiceLocator;
import dev.getelements.elements.sdk.spi.DefaultElementLoaderFactory;
import dev.getelements.elements.sdk.spi.RootElementRegistry;
import dev.getelements.elements.sdk.spi.guice.fixture.unbound.api.UnboundLegacyExportService;
import dev.getelements.elements.sdk.spi.guice.fixture.unbound.provider.ProviderUnboundExportServiceImpl;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.expectThrows;
import static org.testng.Assert.assertTrue;

/**
 * Proves the failure mode from issue #32 can no longer surface as Guice's generic
 * {@code [Guice/ExposedButNotBound]} error: when a LEGACY-strategy element's record exports a
 * service with no {@code @ElementServiceImplementation} (e.g. a broad package scan absorbed a
 * platform export) and no installed {@code @GuiceElementModule} binds it, {@link GuiceSpiModule}
 * now falls back to the parent element registry -- binding the key to the providing element's
 * {@link dev.getelements.elements.sdk.ServiceLocator}, the same mechanism
 * {@code @ElementDependency} services use. If no parent element provides it either, the load
 * fails with a clear {@link SdkException} naming the exact service.
 */
public class GuiceSpiModuleUnboundExportFallbackTest {

    private final DefaultElementLoaderFactory factory = new DefaultElementLoaderFactory();

    @Test
    public void unboundExportResolvesToTheProvidingParentElement() throws Exception {

        final var parent = new RootElementRegistry();

        final var providerRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                ProviderUnboundExportServiceImpl.class.getPackage()
        );

        final var providerInjector = Guice.createInjector(new GuiceSpiModule(parent, providerRecord));
        final var providerLocator = new GuiceServiceLocator();
        providerLocator.setInjector(providerInjector);

        parent.register(new GuiceSdkElement(providerRecord, providerLocator, parent));

        final var recipientPackage = Class
                .forName("dev.getelements.elements.sdk.spi.guice.fixture.unbound.recipient.package-info")
                .getPackage();

        final var recipientRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                recipientPackage
        );

        final var recipientInjector = Guice.createInjector(new GuiceSpiModule(parent, recipientRecord));

        assertEquals(
                recipientInjector.getInstance(UnboundLegacyExportService.class).get(),
                "provider"
        );

        assertEquals(
                recipientInjector.getInstance(Key.get(UnboundLegacyExportService.class, Names.named("named"))).get(),
                "provider"
        );

    }

    @Test
    public void unboundExportWithNoParentProviderRaisesAClearSdkException() throws Exception {

        final var parent = new RootElementRegistry();

        final var recipientPackage = Class
                .forName("dev.getelements.elements.sdk.spi.guice.fixture.unbound.recipient.package-info")
                .getPackage();

        final var recipientRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                recipientPackage
        );

        final var exception = expectThrows(SdkException.class, () ->
                Guice.createInjector(new GuiceSpiModule(parent, recipientRecord)));

        assertTrue(
                exception.getMessage().contains("UnboundLegacyExportService"),
                "the error should name the exact unresolvable service: " + exception.getMessage()
        );

    }

}
