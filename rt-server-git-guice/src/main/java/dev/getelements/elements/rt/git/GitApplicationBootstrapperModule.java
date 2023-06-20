package dev.getelements.elements.rt.git;

import com.google.inject.AbstractModule;
import com.google.inject.PrivateModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.AbstractAssetLoader;
import dev.getelements.elements.rt.ApplicationBootstrapper;
import dev.getelements.elements.rt.ApplicationBootstrapper.BootstrapResources;
import dev.getelements.elements.rt.id.ApplicationId;

import java.util.function.Function;

public class GitApplicationBootstrapperModule extends AbstractModule {

    private Runnable bindResources = () -> {};

    @Override
    protected void configure() {
        bind(ApplicationBootstrapper.class).to(GitApplicationBootstrapper.class);
        bindResources.run();
    }

    public GitApplicationBootstrapperModule withBareRepository() {
        return withResourcesFunction(aid -> BootstrapResources.BARE);
    }

    public GitApplicationBootstrapperModule withResourcesFunction(final Function<ApplicationId, BootstrapResources> function) {

        bindResources = () -> {
            final var tl = new TypeLiteral<Function<ApplicationId, BootstrapResources>>(){};
            bind(tl).toInstance(function);
        };

        return this;

    }

}
