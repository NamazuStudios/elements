package dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.element;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api.PrivateBoundService;
import dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api.PrivateBoundServiceImpl;

import static com.google.inject.name.Names.named;

/**
 * The element's installed {@code @GuiceElementModule}, implemented as a {@link PrivateModule} -- the shape used
 * by the platform's own cloud/internal/conductor modules. Its bindings are invisible to a naive top-level
 * {@code instanceof Binding} walk of {@code Elements.getElements(...)} unless the walker also consults
 * {@code PrivateElements.getExposedKeys()}.
 */
public class PrivateModuleTestFixtureModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(PrivateBoundService.class).to(PrivateBoundServiceImpl.class);
        bind(Key.get(PrivateBoundService.class, named("named"))).to(PrivateBoundServiceImpl.class);
        bind(PrivateBoundServiceImpl.class);
        expose(PrivateBoundService.class);
        expose(Key.get(PrivateBoundService.class, named("named")));
    }

}
