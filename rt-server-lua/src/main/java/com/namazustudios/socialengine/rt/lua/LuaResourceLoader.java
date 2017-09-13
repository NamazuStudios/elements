package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.lua.builtin.AssetLoaderBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.ClasspathBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.ResponseCodeBuiltin;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.rt.lua.IocResolver.IOC_RESOLVER_MODULE_NAME;

public class LuaResourceLoader implements ResourceLoader {

    private AssetLoader assetLoader;

    private Provider<LuaResource> luaResourceProvider;

    private Provider<IocResolver> iocResolverProvider;

    @Override
    public Resource load(final String moduleName, final Object ... args) throws ModuleNotFoundException {
        final LuaResource luaResource = getLuaResourceProvider().get();
        luaResource.installBuiltin(new ClasspathBuiltin());
        luaResource.installBuiltin(new AssetLoaderBuiltin(getAssetLoader()));
        luaResource.installBuiltin(new ResponseCodeBuiltin());
        luaResource.installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME, getIocResolverProvider().get()));
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

    public Provider<IocResolver> getIocResolverProvider() {
        return iocResolverProvider;
    }

    @Inject
    public void setIocResolverProvider(Provider<IocResolver> iocResolverProvider) {
        this.iocResolverProvider = iocResolverProvider;
    }

}
