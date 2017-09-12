package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.lua.LuaResource;

import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

import static com.namazustudios.socialengine.rt.Path.fromPathString;

/**
 * Allows the {@link LuaState} load scripts from the classpath.  The Lua server packge contains a builtin library
 * which scripts may use to assist with interfacing with the rest of the system.
 *
 * Created by patricktwohig on 11/3/15.
 */
public class ClasspathBuiltin implements Builtin {

    public static final String LUA_FILE_EXT = ".lua";

    @Override
    public Module getModuleNamed(final String moduleName) {

        final Path path = fromPathString(moduleName, ".");
        final ClassLoader classLoader = LuaResource.class.getClassLoader();
        final URL resource = classLoader.getResource(path.toNormalizedPathString() + LUA_FILE_EXT);

        return new Module() {
            @Override
            public String getModuleName() {
                return moduleName;
            }

            @Override
            public InputStream getInputStream() throws IOException {
                return resource.openStream();
            }

            @Override
            public boolean exists() {
                return resource != null;
            }

        };

    }

}
