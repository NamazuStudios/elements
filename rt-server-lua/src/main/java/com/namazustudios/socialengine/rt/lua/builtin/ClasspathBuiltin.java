package com.namazustudios.socialengine.rt.lua.builtin;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.lua.Constants;
import com.namazustudios.socialengine.rt.lua.LuaResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    
    private static final Logger logger = LoggerFactory.getLogger(ClasspathBuiltin.class);

    private final ClassLoader classLoader;

    public ClasspathBuiltin() {
        this(LuaResource.class.getClassLoader());
    }

    public ClasspathBuiltin(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

    @Override
    public Module getModuleNamed(final String moduleName) {

        final Path modulePath = fromPathString(moduleName, ".").appendExtension(Constants.LUA_FILE_EXT);
        final URL resource = classLoader.getResource(modulePath.toNormalizedPathString());

        return new Module() {

            @Override
            public String getChunkName() {
                return resource == null ? moduleName : resource.toExternalForm();
            }

            @Override
            public InputStream getInputStream() throws IOException {
                logger.info("Opening resource {}", resource);
                return resource.openStream();
            }

            @Override
            public boolean exists() {
                return resource != null;
            }

        };

    }

}
