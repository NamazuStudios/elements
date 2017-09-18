package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.naef.jnlua.Converter;
import com.naef.jnlua.LuaState;
import com.naef.jnlua.NativeSupport;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import com.namazustudios.socialengine.rt.lua.NativeLibLoader;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends PrivateModule {

    @Override
    protected final void configure() {
        NativeSupport.getInstance().setLoader(new NativeLibLoader());
        configureFeatures();
    }

    /**
     * Configures the features used by this {@Link LuaModule}.  By default this invokes {@link #enableAllFeatures()}.
     * Subclasses may override this method to cherry-pick features they wish to add.
     */
    protected void configureFeatures() {
        enableAllFeatures();
    }

    /**
     * Optionally exposes a binding to {@link LuaState}.  THi
     */
    protected final void exposeLuaState() {
        expose(LuaState.class);
    }

    /**
     * Enables all features, this is the default behavior.
     */
    protected final void enableAllFeatures() {
        enableBasicConverters();
        enableManifestLoaderFeature();
    }

    /**
     * Enables a {@link Converter} which provides automatic conversion of internal features.
     */
    protected final void enableBasicConverters() {
        install(new LuaConverterModule());
    }

    /**
     * Enables configures this {@link LuaModule} to bind and provide the {@link LuaManifestLoader}.  If
     * left blank, then this will not provide the feature and it will be necessary to provide one
     * externally.
     */
    protected final void enableManifestLoaderFeature() {
        install(new LuaManifestLoaderModule());
        expose(ManifestLoader.class);
    }

}
