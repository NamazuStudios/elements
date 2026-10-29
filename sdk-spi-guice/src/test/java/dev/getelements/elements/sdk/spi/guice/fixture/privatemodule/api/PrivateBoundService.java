package dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
@ElementServiceExport(name = "named")
public interface PrivateBoundService {

    String get();

}
