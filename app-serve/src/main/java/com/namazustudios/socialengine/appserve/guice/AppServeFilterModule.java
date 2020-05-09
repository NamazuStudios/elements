package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.appserve.RequestAttributeProfileFilter;
import com.namazustudios.socialengine.appserve.RequestAttributeSessionFilter;
import com.namazustudios.socialengine.appserve.RequestAttributeUserFilter;
import com.namazustudios.socialengine.rt.guice.FilterModule;

public class AppServeFilterModule extends FilterModule {

    @Override
    protected void configureFilters() {
        bindFilter().to(RequestAttributeSessionFilter.class);
        bindFilter().to(RequestAttributeUserFilter.class);
        bindFilter().to(RequestAttributeProfileFilter.class);
    }

}
