package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.jnlua.Converter;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.IocResolver;
import com.namazustudios.socialengine.rt.guice.GuiceIoCResolver;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaResourceLoader;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectModuleBuiltin;

import javax.inject.Provider;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends PrivateModule {

    private Multibinder<Builtin> builtinMultibinder;

    @Override
    protected final void configure() {
        LuaState.logVersionInfo();
        builtinMultibinder = Multibinder.newSetBinder(binder(), Builtin.class);
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
        enableLuaResourceLoaderFeature();
    }

    /**
     * Enables a {@link Converter} which provides automatic conversion of internal features.
     */
    protected final void enableBasicConverters() {
        install(new LuaConverterModule());
    }

    /**
     * Enables configures this {@link LuaModule} to bind and provide the {@link LuaManifestLoader}.  If not called, then
     * this will not provide the feature and it will be necessary to provide one externally.
     */
    protected final void enableManifestLoaderFeature() {
        bind(ManifestLoader.class).to(LuaManifestLoader.class);
        expose(ManifestLoader.class);
    }

    /**
     * Enables configures this {@link LuaModule} to bind and provide the {@link LuaResourceLoader}.  If not called, then
     * this will not provide the feature and it will be necessary to provide one externally.
     */
    protected final void enableLuaResourceLoaderFeature() {
        bind(ResourceLoader.class).to(LuaResourceLoader.class);
        expose(ResourceLoader.class);
    }

    /**
     * Binds a {@link Builtin} to to the type specified by the supplied {@link Class}.
     *
     * @param cls the type
     */
    protected ModuleBinding bindBuiltin(final Class<?> cls) {
        final Provider<?> provider = getProvider(cls);
        return moduleName -> builtinMultibinder.addBinding().toProvider(() -> new JavaObjectModuleBuiltin(moduleName, provider));
    }

    /**
     * Used to add a binding to a Lua module.
     */
    @FunctionalInterface
    public interface ModuleBinding {

        /**
         * Specifies the module.
         *
         * @param moduleName the module name.
         */
        void toModuleNamed(final String moduleName);

    }

}
