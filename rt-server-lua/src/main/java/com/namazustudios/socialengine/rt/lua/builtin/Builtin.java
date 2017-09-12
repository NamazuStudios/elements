package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.lua.StackProtector;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
     * @param name the name of hte {@link Module}
     *
     * @return the {@link Module}, or null if not found
     */
    Module getModuleNamed(String name);

    /**
     * Returns the {@link JavaFunction} representing the searcher function.
     *
     * {@see https://www.lua.org/manual/5.2/manual.html#pdf-require}
     *
     * @return the searcher {@link JavaFunction}
     */
    default JavaFunction getSearcher() {
        return luaState -> {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isString(-1)) {
                    luaState.pushString("module name must be a string");
                    return 1;
                }

                final String moduleName = luaState.checkString(-1);
                final Module module = getModuleNamed(moduleName);

                luaState.setTop(0);

                if (module == null || !module.exists()) {
                    luaState.pushString(moduleName + " not found");
                } else {
                    luaState.pushJavaFunction(getGetLoader());
                    luaState.pushJavaObject(module);
                }

                return stackProtector.setAbsoluteIndex(2);

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
    default JavaFunction getGetLoader() {

        final Logger logger = LoggerFactory.getLogger(getClass());

        return luaState -> {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                final Module module = luaState.checkJavaObject(-1, Module.class);

                final String moduleName = module.getModuleName();
                logger.info("Loading builtin module {} ", moduleName);

                try (final InputStream inputStream = module.getInputStream()) {
                    luaState.load(inputStream, module.getModuleName(), "bt");
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

                logger.info("Successfully parsed builtin module {} ", moduleName);

                luaState.setTop(0);
                luaState.call(0, 1);

                logger.info("Successfully executed module code {} ", moduleName);

                return stackProtector.setAbsoluteIndex(1);

            }
        };
    };

    /**
     * An internal interface to represent a module.  Returns what is needed by the loader {@link JavaFunction} to
     * properly load the script.
     */
    interface Module {

        /**
         * The module name.  This may just parrot back the name supplied to {@link #getModuleNamed(String)}, but may
         * be different.  This may provide more detailed information about the source of the module.
         *
         * @return the module name}
         */
        String getModuleName();

        /**
         * Opens a new {@link InputStream} allowing for a direct read of the underlying asset.
         *
         * @return an {@link InputStream}
         */
        InputStream getInputStream() throws IOException;

        /**
         * Checks if the associated module name exists, useful for avoiding an instance of
         * {@link ModuleNotFoundException}
         *
         * @return true if the module exists, false otherwise
         */
        boolean exists();

    }

}
