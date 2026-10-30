package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.Guice;
import com.google.inject.Key;
import com.google.inject.name.Names;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.spi.DefaultElementLoaderFactory;
import dev.getelements.elements.sdk.spi.RootElementRegistry;
import dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api.PrivateBoundService;
import dev.getelements.elements.sdk.util.SimpleAttributes;
import org.testng.annotations.Test;

import java.util.Map;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;
import static org.testng.Assert.expectThrows;

/**
 * Proves that services bound by an installed {@code @GuiceElementModule} implemented as a
 * {@code com.google.inject.PrivateModule} -- the shape the platform's cloud/internal/conductor modules use --
 * are recognized as bound. {@code Elements.getElements(...)} collapses a private module into a single nested
 * {@code PrivateElements} element whose inner bindings are invisible to a top-level {@code instanceof Binding}
 * filter; missing them made every privately-bound export falsely look unbound, triggering the parent-fallback
 * path and dying with {@code SdkException: ... no installed @GuiceElementModule binds it ...}.
 */
public class GuiceSpiModulePrivateModuleBindingTest {

    private final DefaultElementLoaderFactory factory = new DefaultElementLoaderFactory();

    @Test
    public void privateModuleBindingSatisfiesTheExportWithoutParentFallback() throws Exception {

        final var parent = new RootElementRegistry();

        final var elementPackage = Class
                .forName("dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.element.package-info")
                .getPackage();

        final var elementRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                elementPackage
        );

        // With the bug, construction dies here: the deferred keys look unbound, and with no parent element
        // providing them, findParentProvider throws.

        final var injector = Guice.createInjector(new GuiceSpiModule(parent, elementRecord));

        assertEquals(injector.getInstance(PrivateBoundService.class).get(), "private-module");

        assertEquals(
                injector
                        .getInstance(Key.get(PrivateBoundService.class, Names.named("named")))
                        .get(),
                "private-module"
        );

    }

    @Test
    public void strictValidationAcceptsPrivateModuleBindings() throws Exception {

        final var parent = new RootElementRegistry();

        final var strictPackage = Class
                .forName("dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.strict.package-info")
                .getPackage();

        final var strictRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                strictPackage
        );

        // With the bug, strict validation rejects the privately-bound export despite the installed module
        // binding it.

        final var injector = Guice.createInjector(new GuiceSpiModule(parent, strictRecord));

        assertEquals(injector.getInstance(PrivateBoundService.class).get(), "private-module");

    }

    @Test
    public void genuinelyUnboundExportStillRaisesAClearSdkException() throws Exception {

        final var parent = new RootElementRegistry();

        final var recipientPackage = Class
                .forName("dev.getelements.elements.sdk.spi.guice.fixture.unbound.recipient.package-info")
                .getPackage();

        final var recipientRecord = factory.getElementRecordFromPackage(
                new SimpleAttributes(Map.of()),
                recipientPackage
        );

        // The private-module fix must not overcorrect into treating every key as bound: a truly unbound
        // export still fails with the actionable error from issue #32.

        final var exception = expectThrows(SdkException.class, () ->
                Guice.createInjector(new GuiceSpiModule(parent, recipientRecord)));

        assertTrue(
                exception.getMessage().contains("UnboundLegacyExportService"),
                "the error should name the exact unresolvable service: " + exception.getMessage()
        );

    }

}
