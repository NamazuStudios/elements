package dev.getelements.elements.sdk.spi.guice.fixture.unbound.provider;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;
import dev.getelements.elements.sdk.spi.guice.fixture.unbound.api.UnboundLegacyExportService;

@ElementServiceExport(UnboundLegacyExportService.class)
@ElementServiceExport(value = UnboundLegacyExportService.class, name = "named")
@ElementServiceImplementation
public class ProviderUnboundExportServiceImpl implements UnboundLegacyExportService {

    @Override
    public String get() {
        return "provider";
    }

}
