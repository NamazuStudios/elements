package dev.getelements.elements.rt.git;

import com.google.inject.PrivateModule;
import dev.getelements.elements.rt.ApplicationBootstrapper;

public class GitApplicationBootstrapperModule extends PrivateModule {

    @Override
    protected void configure() {
        bind(ApplicationBootstrapper.class).to(GitApplicationBootstrapper.class);
        expose(ApplicationBootstrapper.class);
    }

}
