package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.ResourceService;

import javax.inject.Qualifier;
import java.lang.annotation.Annotation;

/**
 * Used by Lua resources to access member that would otherwise be provided using
 * the javax.inject annotations {@link javax.inject.Inject} and {@link javax.inject.Named}.
 *
 * Created by patricktwohig on 8/27/15.
 */
public interface IocResolver {

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
     * @param tClass
     * @param named
     * @param <T>
     * @return
     */
    <T> T inject(Class<T> tClass, String named);

}
