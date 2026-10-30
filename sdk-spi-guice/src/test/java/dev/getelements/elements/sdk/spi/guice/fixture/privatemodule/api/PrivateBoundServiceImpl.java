package dev.getelements.elements.sdk.spi.guice.fixture.privatemodule.api;

/**
 * Deliberately unannotated: the service is exported by {@link PrivateBoundService}'s
 * {@code @ElementServiceExport} annotations with no {@code @ElementServiceImplementation} anywhere in the
 * element, so the binding must come from the installed {@code @GuiceElementModule}.
 */
public class PrivateBoundServiceImpl implements PrivateBoundService {

    @Override
    public String get() {
        return "private-module";
    }

}
