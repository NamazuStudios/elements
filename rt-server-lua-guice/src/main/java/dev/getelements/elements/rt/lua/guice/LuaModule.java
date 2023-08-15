package dev.getelements.elements.rt.lua.guice;

import com.google.inject.Key;
import com.google.inject.PrivateModule;
import com.google.inject.multibindings.Multibinder;
import com.namazustudios.socialengine.jnlua.Converter;
import com.namazustudios.socialengine.jnlua.LuaState;
import dev.getelements.elements.rt.Attributes;
import dev.getelements.elements.rt.ManifestLoader;
import dev.getelements.elements.rt.ResourceLoader;
import dev.getelements.elements.rt.SimpleAttributes;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ExposeEnum;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation;
import dev.getelements.elements.rt.annotation.ExposedBindingAnnotation.Undefined;
import dev.getelements.elements.rt.annotation.ModuleDefinition;
import dev.getelements.elements.rt.lua.LuaManifestLoader;
import dev.getelements.elements.rt.lua.LuaResourceLoader;
import dev.getelements.elements.rt.lua.builtin.Builtin;
import dev.getelements.elements.rt.lua.builtin.EnumModuleBuiltin;
import dev.getelements.elements.rt.lua.builtin.JavaObjectModuleBuiltin;
import org.reflections.Reflections;

import javax.inject.Provider;
import javax.ws.rs.client.Client;
import java.util.Set;
import java.util.function.BiConsumer;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

/**
 * Created by patricktwohig on 8/17/17.
 */
public class LuaModule extends PrivateModule {

    private Attributes attributes = Attributes.emptyAttributes();

    private Multibinder<Builtin> builtinMultibinder;

    private BiConsumer<String, Class<?>> legacyVisitors = (s, t) -> {};

    private BiConsumer<ModuleDefinition, Class<?>>  visitors = (e, t) -> {};

    @Override
    protected final void configure() {

        LuaState.logVersionInfo();
        requireBinding(Client.class);

        builtinMultibinder = Multibinder.newSetBinder(binder(), Builtin.class);
        configureFeatures();

        bind(Attributes.class).toProvider(() -> new SimpleAttributes.Builder()
                .from(attributes)
                .build()
        );

    }

    /**
     * Configures the features used by this {@link LuaModule}.  By default this invokes {@link #enableStandardFeatures()}.
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
        return enableJavaExtensions("dev.getelements");
    }

    public LuaModule withAttributes(final Attributes attributes) {
        this.attributes = attributes;
        return this;
    }

    /**
     * Scans for the {@link Expose} and {@link ExposeEnum} annotations to enable any extensions exposed to Lua.
     *
     * @return this instance
     */
    @SuppressWarnings("deprecation")
    public LuaModule enableJavaExtensions(final String packageName) {

        final Reflections reflections = new Reflections(packageName, getClass().getClassLoader());

        // set up the exposed Java classes
        final Set<Class<?>> classSet = reflections.getTypesAnnotatedWith(Expose.class);

        classSet.stream()
            .filter(cls -> cls.getAnnotation(Expose.class) != null)
            .collect(toMap(cls -> cls.getAnnotation(Expose.class), identity()))
            .forEach((expose, type) -> {
                bindModuleBuiltin(type).toModulesNamed(expose.modules());
                bindModuleBuiltin(type).toModulesWithDefinitions(expose.value());
            });

        // set up the exposed Java Enum classes
        final Set<Class<?>> enumClassSet = reflections.getTypesAnnotatedWith(ExposeEnum.class);

        enumClassSet.stream()
            .filter(cls -> cls.getAnnotation(ExposeEnum.class) != null)
            .collect(toMap(cls -> cls.getAnnotation(ExposeEnum.class), identity()))
            .forEach((exposeEnum, type) -> bindEnumModuleBuiltin(type).toModules(exposeEnum.value()));

        return this;

    }

    /**
     * Allows for client code to be made aware of the discovery of an extension.
     *
     * @param visitor the visitor
     * @return this instance
     */
    public LuaModule visitDiscoveredModule(final BiConsumer<ModuleDefinition, Class<?>> visitor) {
        visitors = visitors.andThen(visitor);
        return this;
    }

    /**
     * Allows for client code to be made aware of the discovery of an extension.  This is intended to provide mocks
     * when discovering extensions which may not have already been bound.
     *
     * @param visitor the {@link BiConsumer} used to receive the visited class.
     * @return this instance
     *
     * @deprecated this is for supporting legacy code only
     *
     */
    public LuaModule visitDiscoveredExtension(final BiConsumer<String, Class<?>> visitor) {
        legacyVisitors = legacyVisitors.andThen(visitor);
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

        return new ModuleBinding() {
            @Override
            public LuaModule toModulesNamed(final String[] moduleNames) {

                final var provider = getProvider(cls);

                for (final var moduleName : moduleNames) {
                    legacyVisitors.accept(moduleName, cls);
                    builtinMultibinder.addBinding().toProvider(() -> new JavaObjectModuleBuiltin(moduleName, provider));
                }

                return LuaModule.this;

            }

            @Override
            public LuaModule toModulesWithDefinitions(final ModuleDefinition[] moduleDefinitions) {

                for (final var definition : moduleDefinitions) {

                    final String moduleName = definition.value();

                    final Provider<?> provider;

                    if (Undefined.class.equals(definition.annotation().value())) {
                        provider = getProvider(cls);
                    } else {
                        final var annotation = ExposedBindingAnnotation.Util.resolve(cls, definition.annotation());
                        provider = getProvider(Key.get(cls, annotation));
                    }

                    visitors.accept(definition, cls);
                    builtinMultibinder.addBinding().toProvider(() -> new JavaObjectModuleBuiltin(definition, provider));

                }

                return LuaModule.this;

            }

        };

    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    public EnumModuleBinding bindEnumModuleBuiltin(final Class cls) {
        return new EnumModuleBinding() {

            @Override
            public LuaModule toModules(ModuleDefinition[] moduleDefinitions) {

                for (final var moduleDefinition : moduleDefinitions) {
                    visitors.accept(moduleDefinition, (Class<Object>) cls);
                    builtinMultibinder.addBinding().toProvider(() -> new EnumModuleBuiltin<>(cls, moduleDefinition));
                }

                return LuaModule.this;

            }

            @Override
            public LuaModule toModulesNamed(String[] moduleNames) {

                for (final var moduleName : moduleNames) {
                    legacyVisitors.accept(moduleName, (Class<Object>) cls);
                    builtinMultibinder.addBinding().toProvider(() -> new EnumModuleBuiltin<>(cls, moduleName));
                }

                return LuaModule.this;

            }

        };
    }

    /**
     * Used to add a binding to a Lua module.
     */
    public interface ModuleBinding {

        /**
         * Specifies the module.
         *
         * @param moduleNames the module name.
         */
        LuaModule toModulesNamed(String[] moduleNames);

        /**
         * Specifies the module.
         *
         * @param moduleDefinitions the module name.
         */
        LuaModule toModulesWithDefinitions(ModuleDefinition[] moduleDefinitions);

    }

    public interface EnumModuleBinding {

        /**
         * Specifies the module.
         *
         * @param moduleDefinitions the module name.
         */
        LuaModule toModules(ModuleDefinition[] moduleDefinitions);

        /**
         * Specifies the module.
         *
         * @param moduleNames the module name.
         */
        LuaModule toModulesNamed(String[] moduleNames);

    }

}
