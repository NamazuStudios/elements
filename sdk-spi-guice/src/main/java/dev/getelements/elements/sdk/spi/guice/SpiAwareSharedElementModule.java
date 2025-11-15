package dev.getelements.elements.sdk.spi.guice;

import com.google.inject.TypeLiteral;
import com.google.inject.matcher.Matchers;
import com.google.inject.spi.TypeEncounter;
import com.google.inject.spi.TypeListener;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.sdk.record.ElementDependencyRecord;
import dev.getelements.elements.sdk.spi.guice.record.GuiceElementModuleRecord;

public class SpiAwareSharedElementModule extends SharedElementModule {

    public SpiAwareSharedElementModule(final Package aPackage) {
        super(aPackage);
    }

    public SpiAwareSharedElementModule(final String packageName) {
        super(packageName);
    }

    @Override
    protected void configureElement() {
        GuiceElementModuleRecord
                .fromPackage(aPackage)
                .map(GuiceElementModuleRecord::newModule)
                .forEach(this::install);
    }

}
