package com.namazustudios.socialengine.rt.lua;

import javax.inject.Provider;

/**
 * Used by Lua resources to access member that would otherwise be provided using
 * the javax.inject annotations {@link javax.inject.Inject} and {@link javax.inject.Named}.
 *
 * This module does not have an explicit dependency upon any specific IoC container so
 * this provides a layer of abstraction.
 *
 * Created by patricktwohig on 8/27/15.
 */
public interface IocResolver {

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param className the name of the class
     * @return the instance from the IoC container
     * @throws com.namazustudios.socialengine.exception.InternalException if the class cannot be found
     */
    Object inject(String className);

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param tClass
     * @param <T>
     * @return
     */
    <T> T inject(Class<T> tClass);

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} and {@link javax.inject.Named}
     * annotation.
     *
     * @param className the name of the class
     * @param named the name of the instance
     * @return the instance from the IoC container
     * @throws com.namazustudios.socialengine.exception.InternalException if the class cannot be found
     */
    Object inject(String className, String named);

    /**
     * Gets the type as if it was annotated with the {@link javax.inject.Inject} and {@link javax.inject.Named}
     * annotation.
     *
     * @param tClass the type
     * @param named the name as represented by the {@link javax.inject.Named} annotation
     * @param <T>
     * @return the type
     */
    <T> T inject(Class<T> tClass, String named);

    /**
     * Gets a {@link Provider} for the type as if it was annotated with the {@link javax.inject.Inject} annotation.
     *
     * @param className the name of the class
     * @return the instance from the IoC container
     * @throws com.namazustudios.socialengine.exception.InternalException if the class cannot be found
     */
    Provider<?> provider(String className);

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
     * @throws com.namazustudios.socialengine.exception.InternalException if the class cannot be found
     */
    Provider<?> provider(String className, String named);

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
