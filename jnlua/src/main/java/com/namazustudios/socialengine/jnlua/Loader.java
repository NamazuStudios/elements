package com.namazustudios.socialengine.jnlua;

/**
 * Loads the necessary lua and jnlua libraries.  The {@link DefaultLoader} attempts to load the various libraries
 * from the classpath.  Before using the {@link LuaState}, custom loading code may be swapped out here.
 */
public interface Loader {

    /**
     * Loads the native libraries necessary to support {@link LuaState} and various classes.
     */
    void load();

}
