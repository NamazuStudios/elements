package dev.getelements.elements.service;

import dev.getelements.elements.cdnserve.guice.FileSystemCdnGitLoaderModule;
import dev.getelements.elements.sdk.guice.SharedElementModule;
import dev.getelements.elements.service.guice.ServicesModule;

public class TestServicesModule extends SharedElementModule {

    public TestServicesModule() {
        super("dev.getelements.elements.sdk.service");
    }

    @Override
    protected void configureElement() {
        install(new FileSystemCdnGitLoaderModule());
        install(new ServicesModule(TestScope.scope));
    }

}
