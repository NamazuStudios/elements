package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.lua.builtin.AssetLoaderBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.ClasspathBuiltin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;

public class LuaResourceLoader implements ResourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLoader.class);

    private AssetLoader assetLoader;

    private Provider<LuaResource> luaResourceProvider;

    @Override
    public Resource load(final String moduleName, final Object ... args) throws ModuleNotFoundException {
        final LuaResource luaResource = luaResourceProvider.get();
        luaResource.installBuiltin(new AssetLoaderBuiltin(getAssetLoader()));
        luaResource.installBuiltin(new ClasspathBuiltin());
        return luaResource;
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

}
