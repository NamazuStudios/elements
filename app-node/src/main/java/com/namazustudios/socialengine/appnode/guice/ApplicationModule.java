package com.namazustudios.socialengine.appnode.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.dao.rt.guice.RTFileAssetLoaderModule;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolverModule;
import com.namazustudios.socialengine.rt.lua.guice.LuaModule;
import com.namazustudios.socialengine.rt.remote.InvocationDispatcher;
import com.namazustudios.socialengine.rt.remote.IoCInvocationDispatcher;

import java.io.File;

public class ApplicationModule extends AbstractModule {

    private final File codeDirectory;

    public ApplicationModule(final File codeDirectory) {
        this.codeDirectory = codeDirectory;
    }

    @Override
    protected void configure() {
        install(new LuaModule());
        install(new GuiceIoCResolverModule());
        install(new RTFileAssetLoaderModule(codeDirectory));
        bind(InvocationDispatcher.class).to(IoCInvocationDispatcher.class);

    }

}
