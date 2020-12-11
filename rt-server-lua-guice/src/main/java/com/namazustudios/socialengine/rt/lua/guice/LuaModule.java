package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.jnlua.Converter;
import com.namazustudios.socialengine.jnlua.LuaState;
import com.namazustudios.socialengine.rt.ManifestLoader;
import com.namazustudios.socialengine.rt.ResourceLoader;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposeEnum;
import com.namazustudios.socialengine.rt.lua.LuaManifestLoader;
import com.namazustudios.socialengine.rt.lua.LuaResourceLoader;
import com.namazustudios.socialengine.rt.lua.builtin.Builtin;
import com.namazustudios.socialengine.rt.lua.builtin.EnumModuleBuiltin;
import com.namazustudios.socialengine.rt.lua.builtin.JavaObjectModuleBuiltin;
import org.reflections.Reflections;

import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.stream.Collectors;

import static java.util.function.Function.identity;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends PrivateModule {

    private Multibinder<Builtin> builtinMultibinder;

    private BiConsumer<String, Class<Object>> visitors = (s, t) -> {};

    @Override
    protected final void configure() {

        LuaState.logVersionInfo();
        requireBinding(Client.class);

        builtinMultibinder = Multibinder.newSetBinder(binder(), Builtin.class);
        configureFeatures();

    }

    /**
     * Configures the features used by this {@Link LuaModule}.  By default this invokes {@link #enableStandardFeatures()}.
     * Subclasses may override this method to cherry-pick features they wish to add.
     */
    protected void configureFeatures() {
        enableStandardFeatures();
    }

    /**
     * Enables standard features.
     */
    public LuaModule enableStandardFeatures() {
        enableBasicConverters();
        enableManifestLoaderFeature();
        enableLuaResourceLoaderFeature();
        enableBuiltinJavaExtensions();
        return this;
    }

    /**
     * Enables a {@link Converter} which provides automatic conversion of internal features.
     *
     * @return this instance
     *
     */
    public LuaModule enableBasicConverters() {
        install(new LuaConverterModule());
        return this;
    }

    /**
     * Enables configures this {@link LuaModule} to bind and provide the {@link LuaManifestLoader}.  If not called, then
     * this will not provide the feature and it will be necessary to provide one externally.
     *
     * @return this instance
     *
     */
    public LuaModule enableManifestLoaderFeature() {
        bind(ManifestLoader.class).to(LuaManifestLoader.class);
        expose(ManifestLoader.class);
        return this;
    }

    /**
     * Enables configures this {@link LuaModule} to bind and provide the {@link LuaResourceLoader}.  If not called, then
     * this will not provide the feature and it will be necessary to provide one externally.
     *
     * @return this instance
     *
     */
    public LuaModule enableLuaResourceLoaderFeature() {
        bind(ResourceLoader.class).to(LuaResourceLoader.class);
        expose(ResourceLoader.class);
        return this;
    }

    /**
     * Enables the system-provided extensions using the {@link Expose} annotation.
     *
     * @return this instance
     */
    public LuaModule enableBuiltinJavaExtensions() {
        return enableJavaExtensions("com.namazustudios");
    }

    /**
     * Scans for the {@link Expose} and {@link ExposeEnum} annotations to enable any extensions exposed to Lua.
     *
     * @return this instance
     */
    public LuaModule enableJavaExtensions(final String packageName) {

        final Reflections reflections = new Reflections(packageName, getClass().getClassLoader());

        // set up the exposed Java classes
        final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Expose.class);

        classSet.stream()
            .filter(cls -> cls.getAnnotation(Expose.class) != null)
            .collect(Collectors.toMap(cls -> cls.getAnnotation(Expose.class), identity()))
            .forEach((expose, type) -> bindModuleBuiltin(type).toModulesNamed(expose.modules()));

        // set up the exposed Java Enum classes
        final Set<Class<?>> enumClassSet = reflections.getTypesAnnotatedWith(ExposeEnum.class);

        enumClassSet.stream()
            .filter(cls -> cls.getAnnotation(ExposeEnum.class) != null)
            .collect(Collectors.toMap(cls -> cls.getAnnotation(ExposeEnum.class), identity()))
            .forEach((exposeEnum, type) -> bindEnumModuleBuiltin(type).toModulesNamed(exposeEnum.modules()));

        return this;

    }

    /**
     * Allows for client code to be made aware of the discovery of an extension.  This is intended to provide mocks
     * when discovering extensions which may not have already been bound.
     *
     * @param visitor the {@link BiConsumer} used to receive the visited class.
     * @return this instance
     *
     */
    public LuaModule visitDiscoveredExtension(final BiConsumer<String, Class<Object>> visitor) {
        visitors = visitors.andThen(visitor);
        return this;
    }

    /**
     * Binds a {@link Builtin} to to the type specified by the supplied {@link Class}.  Note that this does not provide
     * the actual binding to the builtin, this merely makes a request for a {@link Provider<T>} which will be used to
     * actually inject the object at a later time.
     *
     * @param cls the type to bind to a {@link Builtin} as a Java module.
     */
    public <T> ModuleBinding bindModuleBuiltin(final Class<T> cls) {

        final Provider<?> provider = getProvider(cls);

        return moduleNames -> {

            for (final String moduleName : moduleNames) {
                visitors.accept(moduleName, (Class<Object>) cls);
                builtinMultibinder.addBinding().toProvider(() -> new JavaObjectModuleBuiltin(moduleName, provider));
            }

            return this;

        };

    }

    public ModuleBinding bindEnumModuleBuiltin(final Class cls) {
        return moduleNames -> {

            for (final String moduleName : moduleNames) {
                visitors.accept(moduleName, (Class<Object>) cls);
                builtinMultibinder.addBinding().toProvider(() -> new EnumModuleBuiltin<>(cls, moduleName));
            }

            return this;

        };

    }

    /**
     * Used to add a binding to a Lua module.
     */
    @FunctionalInterface
    public interface ModuleBinding {

        /**
         * Specifies the module.
         *
         * @param moduleNames the module name.
         */
        LuaModule toModulesNamed(final String[] moduleNames);

    }

}
