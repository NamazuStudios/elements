package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.lua.LogAssist;
import com.sun.jna.Function;
import com.sun.jna.Native;
import com.sun.jna.NativeLibrary;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Field;

import static java.lang.String.format;

/**
 * Loads native Lua libraries from the classpath using JNA.
 */
public class JNABuiltin implements Builtin {

    private static final Logger logger = LoggerFactory.getLogger(JNABuiltin.class);

    @Override
    public Module getModuleNamed(final String moduleName) {
        return new Module() {

            @Override
            public String getChunkName() {
                final NativeLibrary nativeLibrary = NativeLibrary.getInstance(moduleName);
                return nativeLibrary.getFile().getAbsolutePath();
            }

            @Override
            public boolean exists() {

                // This is heavy operation to check and fail.  This should be added at the end of the list of builtins
                // as to avoid expensive checks.

                try {
                    NativeLibrary.getInstance(moduleName);
                    return true;
                } catch (UnsatisfiedLinkError unsatisfiedLinkError) {
                    return false;
                }

            }

        };
    }

    @Override
    public JavaFunction getLoader() {
        return luaState -> {

            final LogAssist logAssist = new LogAssist(() -> logger, () -> luaState);
            final String name = luaState.checkString(1);
            final Module module = luaState.checkJavaObject(2, Module.class);

            final NativeLibrary nativeLibrary = NativeLibrary.getInstance(name);
            logger.info("Loading JNA Lua module {} from {} ", name, module.getChunkName());

            final Function function;
            final String symbolName = format("luaopen_%s", name);

            try {
                function = nativeLibrary.getFunction(symbolName);
            } catch (UnsatisfiedLinkError er) {
                logger.error("Native Lua module does not define {}", symbolName, er);
                logAssist.error("Unsatisfied linkage.", er);
                throw er;
            }

            logger.info("Found loader symbol {} invoking using {}", function.getName(), function.getCallingConvention());

            final long luaStatePointer = luaState.getLuaThread();
            return function.invokeInt(new Object[]{luaStatePointer});

        };
    }

}
