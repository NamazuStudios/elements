@ElementDefinition(
        additionalPackages = @ElementPackage(
                value = "dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api",
                recursive = true
        )
)
@GuiceOptions(strict = true)
@GuiceElementModule(PrivateModuleTestFixtureModule.class)
package dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.strict;

import dev.getelements.elements.sdk.annotation.ElementDefinition;
import dev.getelements.elements.sdk.annotation.ElementPackage;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceElementModule;
import dev.getelements.elements.sdk.spi.guice.annotations.GuiceOptions;
import dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.element.PrivateModuleTestFixtureModule;
