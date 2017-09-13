package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.lua.Constants;

import java.io.InputStream;

import static com.namazustudios.socialengine.rt.Path.fromPathString;

public class AssetLoaderBuiltin implements Builtin {

    private final AssetLoader assetLoader;

    public AssetLoaderBuiltin(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

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
                return assetLoader.open(modulePath);
            }

            @Override
            public boolean exists() {
                return assetLoader.exists(modulePath);
            }

        };

    }

}
