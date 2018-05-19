package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import com.namazustudios.socialengine.rt.lua.persist.Persistence;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

/**
 * Implements a pair of {@link JavaFunction} instances used by lua's require function.
 *
 * {@see https://www.lua.org/manual/5.2/manual.html#pdf-require}
 *
 */
public interface Builtin {

    /**
     * Gets the {@link Module} instance with the provided name.  It the module cannot be found then this
     * may return null.
     *
     * @param moduleName the name of hte {@link Module}
     *
     * @return the {@link Module}, or null if not found
     */
    Module getModuleNamed(String moduleName);

    /**
     * Returns the {@link JavaFunction} representing the searcher function.
     *
     * {@see https://www.lua.org/manual/5.2/manual.html#pdf-require}
     *
     * @return the searcher {@link JavaFunction}
     */
    default JavaFunction getSearcher() {
        return luaState -> {

            final String moduleName = luaState.checkString(1);
            final Module module = getModuleNamed(moduleName);

            final Logger logger = LoggerFactory.getLogger(getClass());
            logger.debug("searching for {} ", moduleName);

            luaState.setTop(0);

            if (module != null && module.exists()) {
                luaState.pushJavaFunction(getLoader());
                luaState.pushJavaObject(module);
                return 2;
            } else {
                luaState.pushString(moduleName + " not found");
                return 1;
            }

        };
    }

    /**
     * Represents the {@link JavaFunction} representing the loader function.
     *
     * {@see https://www.lua.org/manual/5.2/manual.html#pdf-require}
     *
     * @return the loader {@link JavaFunction}
     */
    default JavaFunction getLoader() {

        final Logger logger = LoggerFactory.getLogger(getClass());

        return luaState -> {

            final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);

            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);

            final String chunkName = module.getChunkName();
            logger.debug("Loading builtin module {} -> {} ", name, chunkName);

            try (final InputStream inputStream = module.getInputStream()) {

                luaState.load(inputStream, module.getChunkName(), "bt");
                logger.debug("Successfully parsed builtin module {} ", chunkName);

                luaState.call(0, 1);
                logger.debug("Successfully executed module code {} ", chunkName);

                return 1;

            } catch (IOException ex) {
                logAssist.error("Caught exception loading builtin.", ex);
                logger.info("Caught exception loading builtin module {}.", module.getChunkName(), ex);
                throw new InternalException(ex);
            }

        };

    }

    /**
     * Gets the permanent name of the object so that it can be handled appropriately by the {@link Persistence} class
     * when serializing the underlying {@link LuaResource}.  The default implementation returns the name of this
     * object's {@link Class}.  However, for some implementations, this may not be sufficient.
     *
     * @return the permanent name, must be unique for this {@link Builtin}.
     */
    default String getPermanentName() {
        return getClass().getName();
    }

    /**
     * This makes this {@link Builtin} persistence aware.  This ensures that if the builtin applies custom persistence
     * for any objects that need special care during serialization.  This includes instances of {@link JavaFunction},
     * or {@link Object} types that can't be serialized.
     *
     * The default implementation of this method does nothing assuming the builtin, which is acceptable for those that
     * deal with pure Lua types.
     *
     * @param persistence the {@link Persistence} instance.
     */
    default void makePersistenceAware(Persistence persistence) {}

    /**
     * An internal interface to represent a module.  Returns what is needed by the loader {@link JavaFunction} to
     * properly load the script.
     */
    interface Module {

        /**
         * The chunk name.  This may just parrot back the name supplied to {@link #getModuleNamed(String)}, but may
         * be different.  This may provide more detailed information about the source of the module, such as the
         * full path to the underlying source file as it is loaded.
         *
         * {@see {@link LuaState#load(InputStream, String, String)}
         *
         * @return the chunk name
         */
        String getChunkName();

        /**
         * Opens a new {@link InputStream} allowing for a direct read of the underlying asset.  The default
         * implementation of this method simply throw an instance of {@link FileNotFoundException} to to indicate that
         * the module can't be found.
         *
         * For modules that are backed by a source (such as that from the {@link ClassLoader} or {@link AssetLoader})
         * this must be overridden.
         *
         * @return an {@link InputStream}
         */
        default InputStream getInputStream() throws IOException {
            throw new FileNotFoundException("module has no source.  therefore no stream exists.");
        }

        /**
         * Checks if the associated module name exists, useful for avoiding an instance of
         * {@link ModuleNotFoundException}
         *
         * @return true if the module exists, false otherwise
         */
        boolean exists();

    }

}
