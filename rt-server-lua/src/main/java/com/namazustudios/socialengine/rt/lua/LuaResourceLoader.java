package com.namazustudios.socialengine.rt.lua;

import com.namazustudios.socialengine.rt.*;
import com.namazustudios.socialengine.rt.exception.ModuleNotFoundException;
import com.namazustudios.socialengine.rt.exception.ResourcePersistenceException;
import com.namazustudios.socialengine.rt.lua.builtin.*;
import com.namazustudios.socialengine.rt.lua.persist.PersistenceAwareIocResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.util.Set;

import static com.namazustudios.socialengine.rt.IocResolver.IOC_RESOLVER_MODULE_NAME;

public class LuaResourceLoader implements ResourceLoader {

    private static final Logger logger = LoggerFactory.getLogger(LuaResourceLoader.class);

    private AssetLoader assetLoader;

    private Provider<LuaResource> luaResourceProvider;

    private Provider<IocResolver> iocResolverProvider;

    private Provider<ClasspathBuiltin> classpathBuiltinProvider;

    private Provider<AssetLoaderBuiltin> assetLoaderBuiltinProvider;

    private Provider<ResponseCodeBuiltin> responseCodeBuiltinProvider;

    private Provider<HttpStatusBuiltin> httpStatusBuiltinProvider;

    private Provider<JNABuiltin> jnaBuiltinProvider;

    private Provider<HttpClientBuiltin> httpClientBuiltinProvider;

    private Provider<Set<Builtin>> additionalBuiltins;

    @Override
    public Resource load(final InputStream is, final boolean verbose) throws ResourcePersistenceException {

        final LuaResource luaResource = getLuaResourceProvider().get();
        luaResource.setVerbose(verbose);

        try {

            final IocResolver iocResolver;
            iocResolver = new PersistenceAwareIocResolver(getIocResolverProvider().get(), luaResource.getPersistence());

            luaResource.getBuiltinManager().installBuiltin(new AttributesBuiltin(luaResource::getAttributes));
            luaResource.getBuiltinManager().installBuiltin(getClasspathBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getAssetLoaderBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getResponseCodeBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getHttpStatusBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getHttpClientBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME, iocResolver));
            luaResource.getBuiltinManager().installBuiltin(getJnaBuiltinProvider().get());

            final Set<Builtin> builtinSet = getAdditionalBuiltins().get();
            builtinSet.forEach(luaResource.getBuiltinManager()::installBuiltin);

            luaResource.deserialize(is);

            return luaResource;
        } catch (IOException ex) {
            throw new ResourcePersistenceException(ex);
        } catch (Exception ex) {
            luaResource.close();
            logger.error("Caught exception loading resource.", ex);
            throw ex;
        }

    }

    @Override
    public Resource load(final String moduleName,
                         final Attributes attributes,
                         final Object ... args) throws ModuleNotFoundException {

        final LuaResource luaResource = getLuaResourceProvider().get();
        luaResource.setAttributes(attributes);

        try {

            final IocResolver iocResolver;
            iocResolver = new PersistenceAwareIocResolver(getIocResolverProvider().get(), luaResource.getPersistence());

            luaResource.getBuiltinManager().installBuiltin(new AttributesBuiltin(luaResource::getAttributes));
            luaResource.getBuiltinManager().installBuiltin(getClasspathBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getAssetLoaderBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getResponseCodeBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getHttpStatusBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(getHttpClientBuiltinProvider().get());
            luaResource.getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME, iocResolver));
            luaResource.getBuiltinManager().installBuiltin(getJnaBuiltinProvider().get());

            final Set<Builtin> builtinSet = getAdditionalBuiltins().get();
            builtinSet.forEach(luaResource.getBuiltinManager()::installBuiltin);

            luaResource.loadModule(getAssetLoader(), moduleName, args);

            return luaResource;
        } catch (Exception ex) {
            luaResource.close();
            logger.error("Caught exception loading resource.", ex);
            throw ex;
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

    public Provider<HttpClientBuiltin> getHttpClientBuiltinProvider() {
        return httpClientBuiltinProvider;
    }

    @Inject
    public void setHttpClientBuiltinProvider(Provider<HttpClientBuiltin> httpClientBuiltinProvider) {
        this.httpClientBuiltinProvider = httpClientBuiltinProvider;
    }

    public Provider<Set<Builtin>> getAdditionalBuiltins() {
        return additionalBuiltins;
    }

    @Inject
    public void setAdditionalBuiltins(Provider<Set<Builtin>> additionalBuiltins) {
        this.additionalBuiltins = additionalBuiltins;
    }

}
