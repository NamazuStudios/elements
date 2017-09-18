package com.namazustudios.socialengine.rt.lua;


import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 10/13/15.
 */
public abstract class AbstractIoCResolver implements IocResolver {

    @Override
    public Object inject(final String className) {
        final Class<?> cls;

        try {
            cls = AbstractIoCResolver.class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return inject(cls);
    }

    @Override
    public Object inject(final String className, final String named) {
        final Class<?> cls;

        try {
            cls = AbstractIoCResolver.class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return inject(cls, named);

    }

    @Override
    public Provider<?> provider(String className) {
        final Class<?> cls;

        try {
            cls = AbstractIoCResolver.class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return provider(cls);

    }

    @Override
    public Provider<?> provider(String className, String named) {
        final Class<?> cls;

        try {
            cls = AbstractIoCResolver.class.forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return provider(cls, named);

    }

}
