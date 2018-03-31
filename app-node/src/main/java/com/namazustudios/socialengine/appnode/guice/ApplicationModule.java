package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.annotation.Expose;
import com.namazustudios.socialengine.appnode.ApplicationNodeContext;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.rt.Context;
import com.namazustudios.socialengine.rt.ContextInvocationDispatcher;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.guice.SimpleHandlerContextModule;
import com.namazustudios.socialengine.rt.guice.SimpleResourceContextModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;
import com.namazustudios.socialengine.service.firebase.guice.FirebaseAppFactoryModule;
import com.namazustudios.socialengine.service.notification.guice.GuiceStandardNotificationFactoryModule;
import org.reflections.Reflections;

import java.io.File;
import java.util.Set;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

public class ApplicationModule extends AbstractModule {

    private final File codeDirectory;

    public ApplicationModule(final File codeDirectory) {
        this.codeDirectory = codeDirectory;
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

        install(new GuiceIoCResolverModule());
        install(new SimpleHandlerContextModule());
        install(new SimpleResourceContextModule());
        install(new RTFileAssetLoaderModule(codeDirectory));
        install(new FirebaseAppFactoryModule());

        bind(InvocationDispatcher.class).to(ContextInvocationDispatcher.class);
        bind(Context.class).to(ApplicationNodeContext.class).asEagerSingleton();

    }

}
