package com.namazustudios.socialengine.rt.xodus.provider;

import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class EnvironmentProvider implements Provider<Environment> {

    private static final Logger logger = LoggerFactory.getLogger(EnvironmentProvider.class);

    public static final String ENVIRONMENT_PATH = "com.namazustudios.socialengine.rt.xodus.environment.path";

    private Provider<String> environmentPathProvider;

    @Override
    public Environment get() {
        final String path = getEnvironmentPathProvider().get();
        logger.info("Opening Xodus environment at {}", path);
        return Environments.newInstance(path);
    }

    public Provider<String> getEnvironmentPathProvider() {
        return environmentPathProvider;
    }

    @Inject
    public void setEnvironmentPathProvider(@Named(ENVIRONMENT_PATH) Provider<String> environmentPathProvider) {
        this.environmentPathProvider = environmentPathProvider;
    }

}
