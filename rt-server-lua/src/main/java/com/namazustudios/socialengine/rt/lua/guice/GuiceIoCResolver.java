package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.Injector;
import com.google.inject.Key;
import com.google.inject.name.Names;
import com.namazustudios.socialengine.rt.lua.IocResolver;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 9/1/15.
 */
public class GuiceIoCResolver implements IocResolver {

    @Inject
    private Injector injector;

    @Override
    public <T> T inject(final Class<T> tClass) {
        return injector.getInstance(tClass);
    }

    @Override
    public <T> T inject(final Class<T> tClass, final String named) {
        final Key<T> key = Key.get(tClass, Names.named(named));
        return injector.getInstance(key);
    }

}
