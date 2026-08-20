package dev.getelements.elements.sdk.spi.guice.fixture.guicemoduleonly;

public class GuiceModuleDerivedImpl implements SharedTestService {

    @Override
    public String get() {
        return "guice-module-derived";
    }

}
