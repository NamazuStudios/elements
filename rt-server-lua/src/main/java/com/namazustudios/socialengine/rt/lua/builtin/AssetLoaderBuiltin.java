package com.namazustudios.socialengine.rt.lua.builtin;

import com.namazustudios.socialengine.rt.AssetLoader;
import com.namazustudios.socialengine.rt.Path;

import java.io.InputStream;
import java.util.List;

import static com.namazustudios.socialengine.rt.Path.Util.componentsFromPath;

public class AssetLoaderBuiltin implements Builtin {

    private final AssetLoader assetLoader;

    public AssetLoaderBuiltin(AssetLoader assetLoader) {
        this.assetLoader = assetLoader;
    }

    @Override
    public Module getModuleNamed(final String moduleName) {

        final List<String> components = componentsFromPath(moduleName, ".");
        final Path modulePath = new Path(components);

        return new Builtin.Module() {

            @Override
            public String getModuleName() {
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
