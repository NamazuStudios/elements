package dev.getelements.elements.deployment.jetty.guice;

import dev.getelements.elements.sdk.guice.SharedElementModule;

/**
 * Element module for deployment services. Registers ElementRuntimeService and ElementContainerService
 * implementations as first-class Elements within the sdk.deployment package.
 */
public class DeploymentElementModule extends SharedElementModule {

    public DeploymentElementModule() {
        super("dev.getelements.elements.sdk.deployment");
    }

    @Override
    protected void configureElement() {
        install(new ElementRuntimeServiceModule());
        install(new AppServeModule());
    }

}
