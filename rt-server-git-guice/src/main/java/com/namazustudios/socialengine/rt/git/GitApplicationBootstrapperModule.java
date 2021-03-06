package com.namazustudios.socialengine.rt.git;

import com.google.inject.PrivateModule;
import com.namazustudios.socialengine.rt.ApplicationBootstrapper;

public class GitApplicationBootstrapperModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(ApplicationBootstrapper.class).to(GitApplicationBootstrapper.class);
        expose(ApplicationBootstrapper.class);
    }

}
