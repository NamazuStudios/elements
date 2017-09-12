package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.JavaFunction;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static com.namazustudios.socialengine.rt.Path.Util.componentsFromPath;

public class LuaResourceLoader implements ResourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLoader.class);

    private AssetLoader assetLoader;

    private Provider<LuaResource> luaResourceProvider;

    private JavaFunction loader = luaState -> {

        final Module module = (Module)luaState.toJavaObjectRaw(-1);

        try (final InputStream inputStream = module.getInputStream()) {
            luaState.load(inputStream, module.getModuleName(), "bt");
            return 1;
        } catch (IOException ex) {
            throw new InternalException(ex);
        }

    };

    private JavaFunction searcher = luaState -> {

        final String moduleName = luaState.checkString(-1);
        final Module module = getModuleNamed(moduleName);

        if (module.exists()) {
            luaState.pushJavaFunction(loader);
            luaState.pushJavaObjectRaw(module);
            return 2;
        } else {
            luaState.pushString("Module not found: " + moduleName);
            return 1;
        }

    };

    private Module getModuleNamed(final String moduleName) {

        final List<String> components = componentsFromPath(moduleName, ".");
        final Path modulePath = new Path(components);

        return new Module() {

            @Override
            public String getModuleName() {
                return moduleName;
            }

            @Override
            public InputStream getInputStream() {
                return getAssetLoader().open(modulePath);
            }

            @Override
            public boolean exists() {
                return getAssetLoader().exists(modulePath);
            }

        };

    }

    @Override
    public Resource load(final String moduleName, final Object ... args) throws ModuleNotFoundException {
        final LuaResource luaResource = luaResourceProvider.get();

        return null;
    }

    @Override
    public void close() {
        getAssetLoader().close();
    }

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    public Provider<LuaResource> getLuaResourceProvider() {
        return luaResourceProvider;
    }

    @Inject
    public void setLuaResourceProvider(Provider<LuaResource> luaResourceProvider) {
        this.luaResourceProvider = luaResourceProvider;
    }

    /**
     * An internal interface to represent a module.  Used for loading purposes.
     */
    private interface Module {

        /**
         * The module name.
         *
         * @return the module name}
         */
        String getModuleName();

        /**
         * Opens a new {@link InputStream} allowing for a direct read of the underlying asset.
         *
         * @return an {@link InputStream}
         */
        InputStream getInputStream();

        /**
         * Checks if the associated module name exists, useful for avoiding an instance of
         * {@link ModuleNotFoundException}
         *
         * @return true if the module exists, false otherwise
         */
        boolean exists();

    }

}
