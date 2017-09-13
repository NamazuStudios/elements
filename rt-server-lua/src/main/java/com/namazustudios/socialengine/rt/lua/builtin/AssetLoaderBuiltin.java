package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.lua.Constants;

import javax.inject.Inject;
import java.io.InputStream;

import static com.namazustudios.socialengine.rt.Path.fromPathString;

public class AssetLoaderBuiltin implements Builtin {

    private AssetLoader assetLoader;

    @Override
    public Module getModuleNamed(final String moduleName) {

        final Path modulePath = fromPathString(moduleName, ".").appendExtension(Constants.LUA_FILE_EXT);

        return new Builtin.Module() {

            @Override
            public String getChunkName() {
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

    public AssetLoader getAssetLoader() {
        return assetLoader;
    }

    @Inject
    public void setAssetLoader(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

}
