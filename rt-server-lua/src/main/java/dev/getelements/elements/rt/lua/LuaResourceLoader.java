package dev.getelements.elements.rt.lua;

import dev.getelements.elements.rt.*;
import dev.getelements.elements.rt.exception.ModuleNotFoundException;
import dev.getelements.elements.rt.exception.ResourcePersistenceException;
import dev.getelements.elements.rt.lua.builtin.*;
import dev.getelements.elements.rt.lua.persist.PersistenceAwareIocResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;
import java.nio.channels.ReadableByteChannel;
import java.util.Set;

import static dev.getelements.elements.rt.IocResolver.IOC_RESOLVER_MODULE_NAME;
import static dev.getelements.elements.rt.IocResolver.IOC_RESOLVER_MODULE_NAME_LEGACY;

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

    private Attributes attributes;

    @Override
    public Resource load(final InputStream is, final boolean verbose) throws ResourcePersistenceException {

        final LuaResource luaResource = getLuaResourceProvider().get();

        try {
            preload(luaResource, verbose).deserialize(is);
            return luaResource;
        } catch (IOException ex) {
            luaResource.close();
            throw new ResourcePersistenceException(ex);
        } catch (Exception ex) {
            luaResource.close();
            logger.error("Caught exception loading resource.", ex);
            throw ex;
        }

    }

    @Override
    public Resource load(final ReadableByteChannel rbc, final boolean verbose) throws ResourcePersistenceException {

        final LuaResource luaResource = getLuaResourceProvider().get();

        try {
            preload(luaResource, verbose).deserialize(rbc);
            return luaResource;
        } catch (IOException ex) {
            luaResource.close();
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

        final var luaResource = getLuaResourceProvider().get();

        final var mergedAttributes = new SimpleAttributes.Builder()
                .from(getAttributes())
                .from(attributes)
                .build();

        try {
            preload(luaResource, false);
            luaResource.setAttributes(mergedAttributes);
            luaResource.loadModule(getAssetLoader(), moduleName, args);
            return luaResource;
        } catch (Exception ex) {
            luaResource.close();
            logger.error("Caught exception loading resource.", ex);
            throw ex;
        }

    }

    private LuaResource preload(final LuaResource luaResource, final boolean verbose) {

        luaResource.setVerbose(verbose);

        final IocResolver iocResolver;
        iocResolver = new PersistenceAwareIocResolver(getIocResolverProvider().get(), luaResource.getErisPersistence());

        luaResource.getBuiltinManager().installBuiltin(new AttributesBuiltin(luaResource::getAttributes));
        luaResource.getBuiltinManager().installBuiltin(getClasspathBuiltinProvider().get());
        luaResource.getBuiltinManager().installBuiltin(getAssetLoaderBuiltinProvider().get());
        luaResource.getBuiltinManager().installBuiltin(getResponseCodeBuiltinProvider().get());
        luaResource.getBuiltinManager().installBuiltin(getHttpStatusBuiltinProvider().get());
        luaResource.getBuiltinManager().installBuiltin(getHttpClientBuiltinProvider().get());
        luaResource.getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME, iocResolver));
        luaResource.getBuiltinManager().installBuiltin(new JavaObjectBuiltin<>(IOC_RESOLVER_MODULE_NAME_LEGACY, iocResolver));
        luaResource.getBuiltinManager().installBuiltin(getJnaBuiltinProvider().get());

        final Set<Builtin> builtinSet = getAdditionalBuiltins().get();
        builtinSet.forEach(luaResource.getBuiltinManager()::installBuiltin);

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

    public Attributes getAttributes() {
        return attributes;
    }

    @Inject
    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

}
