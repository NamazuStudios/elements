package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.exception.BaseException;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Provider;

import static java.lang.Class.forName;

/**
 * Used by Lua resources to access member that would otherwise be provided using the javax.inject annotations
 * {@link javax.inject.Inject} and {@link javax.inject.Named}.  This module does not have an explicit dependency upon
 * any specific IoC container, and relies only on those provided by the javax.inject packages.
 *
 * All methods should throw some type of {@link BaseException} wrapping the underlying framework's exception if an
 * injection fails.  In most cases this will map to {@link InternalException}.
 *
 * Created by patricktwohig on 8/27/15.
 */
public interface IocResolver {

    /**
     * The name of the module that will have access to the underlying instance of {@link IocResolver}.
     */
    String IOC_RESOLVER_MODULE_NAME = "namazu.ioc.resolver";

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param className the name of the class
     * @return the instance from the IoC container
     *
     */
    default Object inject(String className) {

        final Class<?> cls;

        try {
            cls = forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return inject(cls);
    }


    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param tClass the type to inject
     * @param <T> the type ot inject
     * @return an injected instance of the supplied {@link Class<T>}
     */
    <T> T inject(Class<T> tClass);

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} and {@link javax.inject.Named}
     * annotation.
     *
     * @param className the name of the class
     * @param named the name of the instance
     * @return the instance from the IoC container
     *
     */
    default Object inject(String className, String named) {
        final Class<?> cls;

        try {
            cls = forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return inject(cls, named);

    }

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} and {@link javax.inject.Named}
     * annotation.
     *
     * @param tClass the type
     * @param named the name as represented by the {@link javax.inject.Named} annotation
     * @param <T>
     *
     * @return the type
     */
    <T> T inject(Class<T> tClass, String named);

    /**
     * Gets a {@link Provider} for the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param className the name of the class
     * @return the instance from the IoC container
     *
     */
    default Provider<?> provider(String className) {
        final Class<?> cls;

        try {
            cls = forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return provider(cls);

    }

    /**
     * Gets a {@link Provider} for the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param tClass the class
     * @param <T> the type
     * @return the {@link Provider} instance
     */
    <T> Provider<T> provider(Class<T> tClass);

    /**
     * Gets a {@link Provider} for  the type as if it was annotated with the {@link javax.inject.Inject}
     * and {@link javax.inject.Named} annotation.
     *
     * @param className the name of the class
     * @param named the name of the instance
     * @return an instance of {@link Provider} of the desired type
     *
     */
    default Provider<?> provider(String className, String named) {
        final Class<?> cls;

        try {
            cls = forName(className);
        } catch (ClassNotFoundException e) {
            throw new InternalException(e);
        }

        return provider(cls, named);

    }

    /**
     * Gets a {@link Provider} for  the type as if it was annotated with the {@link javax.inject.Inject}
     * and {@link javax.inject.Named} annotation.
     *
     * @param tClass the type
     * @param named the name as represented by the {@link javax.inject.Named} annotation
     * @param <T>
     * @return an instance of {@link Provider} of the desired type
     */
    <T> Provider<T> provider(Class<T> tClass, String named);

}
