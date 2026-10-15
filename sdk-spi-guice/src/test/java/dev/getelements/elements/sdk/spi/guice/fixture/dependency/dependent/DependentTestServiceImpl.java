package dev.getelements.elements.sdk.spi.guice.fixture.dependency.dependent;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

@ElementServiceExport(DependentTestService.class)
@ElementServiceImplementation
public class DependentTestServiceImpl implements DependentTestService {

    @Override
    public String get() {
        return "dependent";
    }

}