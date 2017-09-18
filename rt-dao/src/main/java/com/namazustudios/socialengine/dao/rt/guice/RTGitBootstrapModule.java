package com.namazustudios.socialengine.dao.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.TypeLiteral;
import com.namazustudios.socialengine.dao.BootstrapDao;
import com.namazustudios.socialengine.dao.rt.GitBootstrapDao;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.Bootstrapper;
import com.namazustudios.socialengine.rt.lua.LuaBootstrapper;

import java.util.function.Function;

/**
 * Created by patricktwohig on 8/22/17.
 */
public class RTGitBootstrapModule extends AbstractModule {

    @Override
    protected void configure() {

        bind(BootstrapDao.class).to(GitBootstrapDao.class);

        // Right now there is only one bootstrapper, the Lua bootstrapper.  For the sake of simplicity
        // we just return the isntance as requested, but this could be expanded to more langauges or
        // frameworks in the future.

        bind(new TypeLiteral<Function<Application, Bootstrapper>>(){}).toInstance(a -> new LuaBootstrapper());

    }

}
