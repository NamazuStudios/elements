package dev.getelements.elements.rt.xodus;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import dev.getelements.elements.rt.SchedulerEnvironment;
import dev.getelements.elements.rt.util.ProxyDelegate;
import dev.getelements.elements.rt.util.TemporaryFiles;
import dev.getelements.elements.rt.xodus.provider.ResourceEnvironmentProvider;
import dev.getelements.elements.rt.xodus.provider.SchedulerEnvironmentProvider;
import jetbrains.exodus.env.Environment;

import static com.google.inject.name.Names.named;
import static dev.getelements.elements.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT;
import static dev.getelements.elements.rt.xodus.XodusSchedulerEnvironment.SCHEDULER_ENVIRONMENT_PATH;
import static dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT;
import static dev.getelements.elements.rt.xodus.XodusTransactionalResourceServicePersistenceEnvironment.RESOURCE_ENVIRONMENT_PATH;

public class XodusEnvironmentModule extends AbstractModule {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(XodusEnvironmentModule.class);

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
        final var dir = temporaryFiles.createTempDirectory("resource-xodus");
        return withResourceEnvironmentPath(dir.toAbsolutePath().toString());
    }

    public XodusEnvironmentModule withTempSchedulerEnvironment() {
        final var dir = temporaryFiles.createTempDirectory("scheduler-xodus");
        return withSchedulerEnvironmentPath(dir.toAbsolutePath().toString());
    }

    @Override
    protected void configure() {
        
        bindResourceEnvironment.run();
        bindSchedulerEnvironment.run();
        bindResourceEnvironmentPath.run();
        bindSchedulerEnvironmentPath.run();

        bind(SchedulerEnvironment.class).to(XodusSchedulerEnvironment.class);

        bind(new TypeLiteral<ProxyDelegate<Environment>>(){})
            .annotatedWith(named(SCHEDULER_ENVIRONMENT))
            .toProvider(() -> ProxyDelegate.getProxy(Environment.class))
            .asEagerSingleton();

    }

}
