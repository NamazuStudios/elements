package com.namazustudios.socialengine.rt.xodus.provider;

import com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistence;
import jetbrains.exodus.env.Environment;
import jetbrains.exodus.env.Environments;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Provider;

public class ResourceEnvironmentProvider implements Provider<Environment> {

    private static final Logger logger = LoggerFactory.getLogger(ResourceEnvironmentProvider.class);

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
    public void setEnvironmentPathProvider(@Named(XodusTransactionalResourceServicePersistence.RESOURCE_ENVIRONMENT_PATH) Provider<String> environmentPathProvider) {
        this.environmentPathProvider = environmentPathProvider;
    }

}
