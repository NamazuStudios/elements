package dev.getelements.elements.service;

import dev.getelements.elements.cdnserve.guice.FileSystemCdnGitLoaderModule;
import dev.getelements.elements.common.app.ApplicationDeploymentService;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.service.guice.ServicesModule;
import org.mockito.Mockito;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.common.app.ApplicationDeploymentService.APP_SERVE;

public class TestServicesModule extends SharedElementModule {

    public TestServicesModule() {
        super("dev.getelements.elements.sdk.service");
    }

    @Override
    protected void configureElement() {
        install(new FileSystemCdnGitLoaderModule());
        install(new ServicesModule(TestScope.scope));

        bind(ApplicationDeploymentService.class)
                .annotatedWith(named(APP_SERVE))
                .toInstance(Mockito.mock(ApplicationDeploymentService.class));
    }

}
