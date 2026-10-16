package dev.getelements.elements.sdk.spi.guice.fixture.broadscan;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

@ElementServiceExport(BroadScanTestService.class)
@ElementServiceImplementation
public class BroadScanTestServiceImpl implements BroadScanTestService {

    @Override
    public String get() {
        return "broad-scan";
    }

}
