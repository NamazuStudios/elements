package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.namazustudios.socialengine.rt.lua.provider.LuaDozerMapperProvider;
import org.dozer.Mapper;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaDozerModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Mapper.class).toProvider(LuaDozerMapperProvider.class).asEagerSingleton();
    }

}
