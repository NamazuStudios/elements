package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.annotation.Expose;
import com.namazustudios.socialengine.appnode.ApplicationNodeContext;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.ContextInvocationDispatcher;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleHandlerContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleIndexContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.xodus.XodusContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusEnvironmentModule;
import com.namazustudios.socialengine.rt.xodus.XodusResourceContextModule;
import com.namazustudios.socialengine.rt.xodus.XodusServicesModule;
import org.reflections.Reflections;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class ApplicationModule extends AbstractModule {

    private static final String RESOURCES_PATH = "resources";

    private static final String SCHEDULER_PATH = "scheduler";

    private final File codeDirectory;

    private final File storageDirectory;

    public ApplicationModule(final File codeDirectory, final File storageDirectory) {
        this.codeDirectory = codeDirectory;
        this.storageDirectory = storageDirectory;
    }

    @Override
    protected void configure() {

        install(new LuaModule() {
            @Override
            protected void configureFeatures() {
                super.configureFeatures();

                final Reflections reflections = new Reflections("com.namazustudios", getClass().getClassLoader());
                final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Expose.class);

                classSet.stream()
                    .filter(cls -> cls.getAnnotation(Expose.class) != null)
                    .collect(Collectors.toMap(cls -> cls.getAnnotation(Expose.class), identity()))
                    .forEach((expose, type) -> bindBuiltin(type).toModuleNamed(expose.luaModuleName()));

            }
        });

        install(new XodusContextModule());

        final File resources = new File(storageDirectory, RESOURCES_PATH);
        final File scheduler = new File(storageDirectory, SCHEDULER_PATH);

        install(new XodusEnvironmentModule()
            .withResourceEnvironmentPath(resources.getAbsolutePath())
            .withSchedulerEnvironmentPath(scheduler.getAbsolutePath()));

        install(new GuiceIoCResolverModule());
        install(new RTFileAssetLoaderModule(codeDirectory));

        bind(InvocationDispatcher.class).to(ContextInvocationDispatcher.class);

    }

}
