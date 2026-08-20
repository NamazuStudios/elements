package dev.getelements.elements.sdk.spi.guice.fixture.guicemoduleonly;

import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.annotation.ElementServiceImplementation;

/**
 * Annotated as though it were the implementation the legacy annotation-driven scan would auto-bind. Under
 * {@code GuiceOptions.LoadingStrategy.GUICE_MODULE_ONLY} this must be ignored entirely in favor of whatever
 * {@link GuiceModuleOnlyTestFixtureModule} binds -- if it were not ignored, installing both would conflict
 * (Guice does not allow the same key to be bound twice).
 */
@ElementServiceExport(SharedTestService.class)
@ElementServiceImplementation
public class AnnotationDerivedImpl implements SharedTestService {

    @Override
    public String get() {
        return "annotation-derived";
    }

}
