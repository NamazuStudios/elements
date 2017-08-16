package com.namazustudios.socialengine.rt.lua;

import com.naef.jnlua.LuaState;
import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.rt.manifest.http.HttpManifest;

import javax.inject.Inject;
import javax.inject.Provider;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by patricktwohig on 8/15/17.
 */
public class LuaManifestLoader implements ManifestLoader {

    public static final String MAIN_MANIFEST = "main.lua";

    private AssetLoader assetLoader;

    private Provider<LuaState> luaStateProvider;

    @Override
    public HttpManifest getHttpManifest() {
        return null;
    }

    @Override
    public void close() {

    }

    public LuaState loadStateForPath(final String path) {
        try (final InputStream inputStream = getAssetLoader().open(path)) {
            final LuaState luaState = getLuaStateProvider().get();
            luaState.load(inputStream, path, "bt");
            return luaState;
        } catch (IOException ex) {
            throw new InternalException(ex);
        }
    }


    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    public Provider<LuaState> getLuaStateProvider() {
        return luaStateProvider;
    }

    @Inject
    public void setLuaStateProvider(Provider<LuaState> luaStateProvider) {
        this.luaStateProvider = luaStateProvider;
    }

}
