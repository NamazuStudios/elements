package com.namazustudios.socialengine.rt.xodus;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.xodus.provider.EnvironmentProvider;
import jetbrains.exodus.env.Environment;

import java.io.File;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.xodus.provider.EnvironmentProvider.ENVIRONMENT_PATH;

public class XodusEnvironmentModule extends AbstractModule {

    private Runnable bindEnvironmentPath = () -> {};

    public XodusEnvironmentModule withTempEnvironment() {
        final File temp = Files.createTempDir();
        return withEnvironmentPath(temp.getAbsolutePath());
    }

    public XodusEnvironmentModule withEnvironmentPath(final String path) {
        bindEnvironmentPath = () -> bind(String.class)
            .annotatedWith(named(ENVIRONMENT_PATH))
            .toInstance(path);
        return this;
    }

    @Override
    protected void configure() {
        bindEnvironmentPath.run();
        bind(Environment.class).toProvider(EnvironmentProvider.class);
    }

}
