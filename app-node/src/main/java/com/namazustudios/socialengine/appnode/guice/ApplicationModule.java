package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.ContextLocalInvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.LocalInvocationDispatcher;

import javax.inject.Provider;
import java.io.File;

public class ApplicationModule extends AbstractModule {

    private static final String RESOURCES_PATH = "resources";

    private static final String SCHEDULER_PATH = "scheduler";

    private final Application application;

    private final File codeDirectory;

    private final File storageDirectory;

    public ApplicationModule(final Application application,
                             final File codeDirectory, final File storageDirectory) {
        this.application = application;
        this.codeDirectory = codeDirectory;
        this.storageDirectory = storageDirectory;
    }

    @Override
    protected void configure() {

        install(new LuaModule());
//        install(new XodusContextModule());

        final File resources = new File(storageDirectory, RESOURCES_PATH);
        final File scheduler = new File(storageDirectory, SCHEDULER_PATH);

//        install(new XodusEnvironmentModule()
//            .withResourceEnvironmentPath(resources.getAbsolutePath())
//            .withSchedulerEnvironmentPath(scheduler.getAbsolutePath()));

        install(new GuiceIoCResolverModule());
        install(new RTFileAssetLoaderModule(codeDirectory));

        bind(LocalInvocationDispatcher.class).to(ContextLocalInvocationDispatcher.class);

        final String applicationId = application.getId();
        final Provider<ApplicationDao> applicationDaoProvider = getProvider(ApplicationDao.class);
        bind(Application.class).toProvider(() -> applicationDaoProvider.get().getActiveApplication(applicationId));

    }

}
