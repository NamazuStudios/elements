package dev.getelements.elements.cdnserve;

import com.google.inject.AbstractModule;
import dev.getelements.elements.cdnserve.api.DeploymentService;
import dev.getelements.elements.cdnserve.api.DeploymentServiceProvider;

import static com.google.inject.servlet.ServletScopes.REQUEST;

public class CdnServicesModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(DeploymentService.class).toProvider(DeploymentServiceProvider.class).in(REQUEST);
    }

}
