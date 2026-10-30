package dev.getelements.elements.sdk.spi.guice.fixture.dependency.provider;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

@ElementServiceExport(DependencyTestService.class)
@ElementServiceImplementation
public class DependencyTestServiceImpl implements DependencyTestService {

    @Override
    public String get() {
        return "provider";
    }

}