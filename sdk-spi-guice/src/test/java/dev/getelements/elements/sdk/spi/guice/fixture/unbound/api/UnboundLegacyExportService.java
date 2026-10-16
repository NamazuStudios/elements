package dev.getelements.elements.sdk.spi.guice.fixture.unbound.api;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

@ElementServiceExport
@ElementServiceExport(name = "named")
public interface UnboundLegacyExportService {

    String get();

}
