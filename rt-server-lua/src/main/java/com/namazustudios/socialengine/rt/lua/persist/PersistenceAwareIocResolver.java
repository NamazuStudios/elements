package com.namazustudios.socialengine.rt.lua.persist;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.IocResolver;

import javax.inject.Inject;
import javax.inject.Provider;


/**
 * Delegates to another instance of {@link IocResolver}.
 */
public class PersistenceAwareIocResolver implements IocResolver {

    private final IocResolver delegate;

    private final Persistence persistence;

    private static final String TYPE = "_t";

    private static final String NAME = "_n";

    private static final String INJECT_TYPE = Inject.class.getName();

    private static final String PROVIDER_TYPE = Provider.class.getName();

    public PersistenceAwareIocResolver(final IocResolver delegate, final Persistence persistence) {

        this.delegate = delegate;
        this.persistence = persistence;

        persistence.addCustomUnpersistence(INJECT_TYPE, l -> {

            if (l.getTop() != 1) {
                throw new IllegalArgumentException("Function expects exactly 1 argument.");
            }

            l.getField(1, TYPE);
            l.getField(1, NAME);

            final String type = l.toString(2);
            final String name = l.toString(3);

            if (name == null) {
                final Object injectee = inject(type);
                l.pushJavaObject(injectee);
            } else {
                final Object injectee = inject(type, name);
                l.pushJavaObject(injectee);
            }

            return 1;

        });

        persistence.addCustomUnpersistence(PROVIDER_TYPE, l -> {

            if (l.getTop() != 1) {
                throw new IllegalArgumentException("Function expects exactly 1 argument.");
            }

            l.getField(1, TYPE);
            l.getField(1, NAME);

            final String type = l.toString(2);
            final String name = l.toString(3);

            if (name == null) {
                final Provider<?> provider = provider(type);
                l.pushJavaObject(provider);
            } else {
                final Provider<?> provider = provider(type, name);
                l.pushJavaObject(provider);
            }

            return 1;

        });

    }

    @Override
    public <T> T inject(final Class<T> tClass) {
        final T instance = getDelegate().inject(tClass);
        getPersistence().addCustomPersistence(instance, INJECT_TYPE, l -> persist(l, tClass));
        return instance;
    }

    @Override
    public <T> T inject(final Class<T> tClass, final String named) {
        final T instance = getDelegate().inject(tClass, named);
        getPersistence().addCustomPersistence(instance, INJECT_TYPE, l -> persist(l, tClass, named));
        return instance;
    }

    @Override
    public <T> Provider<T> provider(final Class<T> tClass) {
        final Provider<T> provider = getDelegate().provider(tClass);
        getPersistence().addCustomPersistence(provider, INJECT_TYPE, l -> persist(l, tClass));
        return provider;
    }

    @Override
    public <T> Provider<T> provider(final Class<T> tClass, final String named) {
        final Provider<T> provider = getDelegate().provider(tClass);
        getPersistence().addCustomPersistence(provider, INJECT_TYPE, l -> persist(l, tClass, named));
        return provider;
    }

    private int persist(final LuaState l, final Class<?> tClass) {

        if (l.getTop() != 1) {
            throw new IllegalArgumentException("Function expects exactly 1 argument.");
        }

        l.newTable();
        l.pushString(TYPE);
        l.pushString(tClass.getName());
        l.setTable(-3);

        return 1;

    }

    private int persist(final LuaState l, final Class<?> tClass, final String named) {

        if (l.getTop() != 1) {
            throw new IllegalArgumentException("Function expects exactly 1 argument.");
        }

        l.newTable();
        l.pushString(TYPE);
        l.pushString(tClass.getName());
        l.setTable(-3);

        l.pushString(NAME);
        l.pushString(named);
        l.setTable(-3);

        return 1;

    }


    public IocResolver getDelegate() {
        return delegate;
    }

    public Persistence getPersistence() {
        return persistence;
    }

}
