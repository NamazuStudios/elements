package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.exception.InternalException;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Created by patricktwohig on 11/3/15.
 */
public class ClasspathModuleLoader {

    private final AbstractLuaResource abstractLuaResource;

    private final JavaFunction classpathSearcher = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                if (!luaState.isString(-1)) {
                    luaState.pushString("module name must be a string");
                    return 1;
                }

                final String moduleName = luaState.checkString(-1);
                final ClassLoader classLoader = AbstractLuaResource.class.getClassLoader();
                final URL resourceURL = classLoader.getResource(moduleName + ".lua");

                luaState.setTop(0);

                if (resourceURL == null) {
                    luaState.pushString(moduleName + " not found on classpath");
                } else {
                    luaState.pushJavaFunction(classpathLoader);
                    luaState.pushJavaObject(resourceURL);
                }

                return stackProtector.setAbsoluteIndex(2);

            }
        }
    };

    private final JavaFunction classpathLoader = new JavaFunction() {
        @Override
        public int invoke(final LuaState luaState) {
            try (final StackProtector stackProtector = new StackProtector(luaState)) {

                final URL resourceURL = luaState.checkJavaObject(-1, URL.class);
                final String simpleFileName = AbstractLuaResource.simlifyFileName(resourceURL.getFile());
                abstractLuaResource.getScriptLog().debug("Loading module from {}", resourceURL);

                try (final InputStream inputStream = resourceURL.openStream())  {
                    luaState.load(inputStream, simpleFileName, "bt");
                } catch (IOException ex) {
                    throw new InternalException(ex);
                }

                luaState.remove(-2);
                luaState.remove(-2);
                luaState.call(0, 1);

                return stackProtector.setAbsoluteIndex(1);

            }
        }
    };

    public ClasspathModuleLoader(AbstractLuaResource abstractLuaResource) {
        this.abstractLuaResource = abstractLuaResource;
    }

    public void setup() {
        final LuaState luaState = abstractLuaResource.getLuaState();
        try (final StackProtector stackProtector = new StackProtector(luaState)) {
            luaState.getGlobal(Constants.PACKAGE_TABLE);
            luaState.getField(-1, Constants.PACKAGE_SEARCHERS_TABLE);
            luaState.pushJavaFunction(classpathSearcher);
            luaState.rawSet(-2, luaState.rawLen(-1) + 1);
            luaState.pop(2);
        }
    }

}
