package dev.getelements.elements.appserve.guice;

import dev.getelements.elements.appserve.RequestAttributeProfileFilter;
import dev.getelements.elements.appserve.RequestAttributeSessionFilter;
import dev.getelements.elements.appserve.RequestAttributeUserFilter;
import dev.getelements.elements.rt.guice.FilterModule;

@Deprecated
public class AppServeFilterModule extends FilterModule {

    @Override
    protected void configureFilters() {
        bindFilter().to(RequestAttributeSessionFilter.class);
        bindFilter().to(RequestAttributeUserFilter.class);
        bindFilter().to(RequestAttributeProfileFilter.class);
    }

}
