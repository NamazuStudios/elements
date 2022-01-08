package com.namazustudios.socialengine.rt.xodus;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.util.TestTemporaryFiles;
import com.namazustudios.socialengine.rt.xodus.provider.ResourceEnvironmentProvider;
import com.namazustudios.socialengine.rt.xodus.provider.SchedulerEnvironmentProvider;
import jetbrains.exodus.env.Environment;

import java.io.IOException;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerContext.SCHEDULER_ENVIRONMENT;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT;
import static com.namazustudios.socialengine.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT_PATH;
import static java.nio.file.Files.createTempDirectory;

public class XodusEnvironmentModule extends AbstractModule {

    private Runnable bindResourceEnvironment = () -> {};

    private Runnable bindSchedulerEnvironment = () -> {};

    private Runnable bindResourceEnvironmentPath = () -> {};

    private Runnable bindSchedulerEnvironmentPath = () -> {};

    public XodusEnvironmentModule withResourceEnvironment() {
        bindResourceEnvironment = () -> bind(Environment.class)
            .annotatedWith(named(RESOURCE_ENVIRONMENT))
            .toProvider(ResourceEnvironmentProvider.class);
        return this;
    }

    public XodusEnvironmentModule withSchedulerEnvironment() {
        bindSchedulerEnvironment = () -> bind(Environment.class)
            .annotatedWith(named(SCHEDULER_ENVIRONMENT))
            .toProvider(SchedulerEnvironmentProvider.class);
        return this;
    }

    public XodusEnvironmentModule withResourceEnvironmentPath(final String path) {
        bindResourceEnvironmentPath = () -> bind(String.class)
            .annotatedWith(named(RESOURCE_ENVIRONMENT_PATH))
            .toInstance(path);
        return withResourceEnvironment();
    }

    public XodusEnvironmentModule withSchedulerEnvironmentPath(final String path) {
        bindSchedulerEnvironmentPath = () -> bind(String.class)
            .annotatedWith(named(SCHEDULER_ENVIRONMENT_PATH))
            .toInstance(path);
        return withSchedulerEnvironment();
    }

    public XodusEnvironmentModule withTempResourceEnvironment() {
        try {
            final var dir = TestTemporaryFiles.getDefaultInstance().createTempDirectory("resource-xodus");
            return withResourceEnvironmentPath(dir.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    public XodusEnvironmentModule withTempSchedulerEnvironment() {
        try {
            final var dir = TestTemporaryFiles.getDefaultInstance().createTempDirectory("scheduler-xodus");
            return withSchedulerEnvironmentPath(dir.toAbsolutePath().toString());
        } catch (IOException e) {
            throw new InternalException(e);
        }
    }

    @Override
    protected void configure() {
        bindResourceEnvironment.run();
        bindSchedulerEnvironment.run();
        bindResourceEnvironmentPath.run();
        bindSchedulerEnvironmentPath.run();
    }

}
