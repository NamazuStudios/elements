package com.namazustudios.socialengine.appserve.guice;

import com.namazustudios.socialengine.appserve.RequestAttributeProfileFilter;
import com.namazustudios.socialengine.appserve.SessionIdAuthenticationFilter;
import com.namazustudios.socialengine.rt.guice.FilterModule;

public class AppServeFilterModule extends FilterModule {

    @Override
    protected void configureFilters() {
        bindFilter().to(SessionIdAuthenticationFilter.class);
        bindFilter().to(RequestAttributeProfileFilter.class);
    }

}
