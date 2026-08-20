@ElementDefinition
@GuiceOptions(strategy = GuiceOptions.LoadingStrategy.GUICE_MODULE_ONLY)
@GuiceElementModule(GuiceModuleOnlyTestFixtureModule.class)
package dev.getelements.elements.sdk.spi.guice.fixture.guicemoduleonly;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceElementModule;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions;
