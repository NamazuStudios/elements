package com.namazustudios.socialengine.rt.xodus;

import com.google.common.io.Files;
import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.xodus.provider.ResourceEnvironmentProvider;
import com.namazustudios.socialengine.rt.xodus.provider.SchedulerEnvironmentProvider;
import jetbrains.exodus.env.Environment;

import static com.google.inject.name.Names.named;
import static com.namazustudios.socialengine.rt.xodus.XodusResourceService.RESOURCE_ENVIRONMENT;
import static com.namazustudios.socialengine.rt.xodus.XodusSchedulerContext.SCHEDULER_ENVIRONMENT;
import static com.namazustudios.socialengine.rt.xodus.provider.ResourceEnvironmentProvider.RESOURCE_ENVIRONMENT_PATH;
import static com.namazustudios.socialengine.rt.xodus.provider.SchedulerEnvironmentProvider.SCHEDULER_ENVIRONMENT_PATH;

public class XodusEnvironmentModule extends AbstractModule {

    private Runnable bindResourceEnvironment = () -> {};

    private Runnable bindSchedulerEnvironment = () -> {};

    private Runnable bindResourceEnvironmentPath = () -> {};

    private Runnable bindSchedulerEnvironmentPath = () -> {};

    public XodusEnvironmentModule withResourceEnvironment() {
        bindResourceEnvironment = () -> bind(Environment.class)
            .annotatedWith(named(RESOURCE_ENVIRONMENT))
            .toProvider(ResourceEnvironmentProvider.class)
            .asEagerSingleton();
        return this;
    }

    public XodusEnvironmentModule withSchedulerEnvironment() {
        bindSchedulerEnvironment = () -> bind(Environment.class)
            .annotatedWith(named(SCHEDULER_ENVIRONMENT))
            .toProvider(SchedulerEnvironmentProvider.class)
            .asEagerSingleton();
        return this;
    }

    public XodusEnvironmentModule withTempEnvironments() {
        return withResourceEnvironmentPath(Files.createTempDir().getAbsolutePath()).
               withSchedulerEnvironmentPath(Files.createTempDir().getAbsolutePath());
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

    @Override
    protected void configure() {
        bindResourceEnvironment.run();
        bindSchedulerEnvironment.run();
        bindResourceEnvironmentPath.run();
        bindSchedulerEnvironmentPath.run();
    }

}
