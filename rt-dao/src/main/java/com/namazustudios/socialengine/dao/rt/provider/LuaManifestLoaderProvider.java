package com.namazustudios.socialengine.dao.rt.provider;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.model.application.Application;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.function.Function;

/**
 * Created by patricktwohig on 8/19/17.
 */
public class LuaManifestLoaderProvider implements Provider<Function<Application, ManifestLoader>> {

    private static final Logger logger = LoggerFactory.getLogger(LuaManifestLoaderProvider.class);

    private Provider<LuaState> luaStateProvider;

    private Provider<Function<Application, AssetLoader>> applicationAssetLoaderFunctionProvider;

    public LuaManifestLoaderProvider() {
        logger.info("Using Lua Manifest Loader Provider.");
    }

    @Override
    public Function<Application, ManifestLoader> get() {
        return application -> {

            final LuaManifestLoader luaManifestLoader = new LuaManifestLoader();
            final AssetLoader assetLoader = getApplicationAssetLoaderFunctionProvider().get().apply(application);

            luaManifestLoader.setAssetLoader(assetLoader);
            luaManifestLoader.setLuaStateProvider(getLuaStateProvider());

            return luaManifestLoader;

        };
    }

    public Provider<LuaState> getLuaStateProvider() {
        return luaStateProvider;
    }

    @Inject
    public void setLuaStateProvider(Provider<LuaState> luaStateProvider) {
        this.luaStateProvider = luaStateProvider;
    }

    public Provider<Function<Application, AssetLoader>> getApplicationAssetLoaderFunctionProvider() {
        return applicationAssetLoaderFunctionProvider;
    }

    @Inject
    public void setApplicationAssetLoaderFunctionProvider(Provider<Function<Application, AssetLoader>> applicationAssetLoaderFunctionProvider) {
        this.applicationAssetLoaderFunctionProvider = applicationAssetLoaderFunctionProvider;
    }

}
