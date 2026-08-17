package dev.getelements.elements.sdk.spi.guice.fixture.guicemoduleonly;

import com.google.inject.AbstractModule;

public class GuiceModuleOnlyTestFixtureModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GuiceModuleDerivedImpl.class);
        bind(SharedTestService.class).to(GuiceModuleDerivedImpl.class);
    }

}
