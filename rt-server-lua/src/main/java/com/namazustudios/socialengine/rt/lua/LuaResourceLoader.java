package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Resource;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.lua.builtin.*;

import javax.inject.Inject;
import javax.inject.Provider;

import static com.namazustudios.socialengine.rt.lua.IocResolver.IOC_RESOLVER_MODULE_NAME;

public class LuaResourceLoader implements ResourceLoader {

    private AssetLoader assetLoader;

    private Provider<LuaResource> luaResourceProvider;

    private Provider<IocResolver> iocResolverProvider;

    private Provider<ClasspathBuiltin> classpathBuiltinProvider;

    private Provider<AssetLoaderBuiltin> assetLoaderBuiltinProvider;

    private Provider<ResponseCodeBuiltin> responseCodeBuiltinProvider;

    private Provider<HttpStatusBuiltin> httpStatusBuiltinProvider;

    private Provider<JNABuiltin> jnaBuiltinProvider;

    @Override
    public Resource load(final String moduleName, final Object ... args) throws ModuleNotFoundException {

        final LuaResource luaResource = getLuaResourceProvider().get();

        try {
            final IocResolver iocResolver = getIocResolverProvider().get();
            luaResource.getBuiltinManager().installBuiltin(getClasspathBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getAssetLoaderBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getResponseCodeBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getHttpStatusBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME, iocResolver));
            luaResource.getBuiltinManager().installBuiltin(getJnaBuiltinProvider().get());
            luaResource.loadModule(getAssetLoader(), moduleName, args);
            return luaResource;
        } catch (Throwable th) {
            luaResource.close();
            throw th;
        }

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

    public Provider<AssetLoaderBuiltin> getAssetLoaderBuiltinProvider() {
        return assetLoaderBuiltinProvider;
    }

    @Inject
    public void setAssetLoaderBuiltinProvider(Provider<AssetLoaderBuiltin> assetLoaderBuiltinProvider) {
        this.assetLoaderBuiltinProvider = assetLoaderBuiltinProvider;
    }

    public Provider<ResponseCodeBuiltin> getResponseCodeBuiltinProvider() {
        return responseCodeBuiltinProvider;
    }

    @Inject
    public void setResponseCodeBuiltinProvider(Provider<ResponseCodeBuiltin> responseCodeBuiltinProvider) {
        this.responseCodeBuiltinProvider = responseCodeBuiltinProvider;
    }

    public Provider<ClasspathBuiltin> getClasspathBuiltinProvider() {
        return classpathBuiltinProvider;
    }

    @Inject
    public void setClasspathBuiltinProvider(Provider<ClasspathBuiltin> classpathBuiltinProvider) {
        this.classpathBuiltinProvider = classpathBuiltinProvider;
    }

    public Provider<HttpStatusBuiltin> getHttpStatusBuiltinProvider() {
        return httpStatusBuiltinProvider;
    }

    @Inject
    public void setHttpStatusBuiltinProvider(Provider<HttpStatusBuiltin> httpStatusBuiltinProvider) {
        this.httpStatusBuiltinProvider = httpStatusBuiltinProvider;
    }

    public Provider<JNABuiltin> getJnaBuiltinProvider() {
        return jnaBuiltinProvider;
    }

    @Inject
    public void setJnaBuiltinProvider(Provider<JNABuiltin> jnaBuiltinProvider) {
        this.jnaBuiltinProvider = jnaBuiltinProvider;
    }

}
