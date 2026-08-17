package dev.getelements.elements.sdk.spi.guice.fixture.legacy;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

@ElementServiceExport(LegacyTestService.class)
@ElementServiceImplementation
public class LegacyTestServiceImpl implements LegacyTestService {

    @Override
    public String get() {
        return "legacy";
    }

}
