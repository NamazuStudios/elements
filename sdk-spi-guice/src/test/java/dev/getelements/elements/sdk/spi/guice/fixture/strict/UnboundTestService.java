package dev.getelements.elements.sdk.spi.guice.fixture.strict;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;

/**
 * Exported but deliberately never bound anywhere (no {@code @ElementServiceImplementation}, no
 * {@code @GuiceElementModule} on the containing package) -- exercises {@code GuiceOptions.strict()}'s validation.
 */
@ElementServiceExport
public interface UnboundTestService {

    String get();

}
