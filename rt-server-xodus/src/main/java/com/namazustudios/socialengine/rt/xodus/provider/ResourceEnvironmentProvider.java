package com.namazustudios.socialengine.rt.xodus.provider;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ResourceEnvironmentProvider implements Provider<Environment> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceEnvironmentProvider.class);

    public static final String RESOURCE_ENVIRONMENT = "com.namazustudios.socialengine.rt.xodus.resource";

    public static final String RESOURCE_ENVIRONMENT_PATH = "com.namazustudios.socialengine.rt.xodus.environment.path";


    private Provider<String> environmentPathProvider;

    @Override
    public Environment get() {
        final String path = getEnvironmentPathProvider().get();
        logger.info("Opening Xodus environment for Resources at {}", path);
        return Environments.newInstance(path);
    }

    public Provider<String> getEnvironmentPathProvider() {
        return environmentPathProvider;
    }

    @Inject
    public void setEnvironmentPathProvider(@Named(RESOURCE_ENVIRONMENT_PATH) Provider<String> environmentPathProvider) {
        this.environmentPathProvider = environmentPathProvider;
    }

}
