//package com.namazustudios.socialengine.rt.lua.guice;
//
//import com.google.inject.AbstractModule;
//import com.google.inject.TypeLiteral;
//import com.google.inject.multibindings.MapBinder;
//import com.google.inject.name.Names;
//import com.google.inject.util.Providers;
//import com.namazustudios.socialengine.rt.Path;
//import com.namazustudios.socialengine.rt.handler.Handler;
//import com.namazustudios.socialengine.rt.worker.Worker;
//import com.namazustudios.socialengine.rt.lua.*;
//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;
//
//import javax.inject.Provider;
//import java.io.File;
//
//import static com.namazustudios.socialengine.rt.lua.InternalResourceProviders.classpathProviderForScript;
//import static com.namazustudios.socialengine.rt.lua.InternalResourceProviders.filesystemProviderForScript;
//
//
///**
// * Allows for client code to manually bind Lua scripts with names.  Once bound, they can
// * be accessed through the {@link GuiceIoCResolver}.
// *
// * Created by patricktwohig on 9/1/15.
// */
//public abstract class LuaResourceModule extends AbstractModule {
//
//    private static final Logger LOG = LoggerFactory.getLogger(LuaResourceModule.class);
//
//    private MapBinder<Path, Handler> bootstrapEdgeResources;
//
//    @Override
//    protected final void configure() {
//
//        LOG.info("Loading Lua modules.  java.library.path is {}", System.getProperty("java.library.path"));
//
//        bootstrapEdgeResources = MapBinder.newMapBinder(binder(),
//            new TypeLiteral<Path>(){},
//            new TypeLiteral<Handler>(){});
//
//        binder().bind(Tabler.class)
//                .to(DefaultTabler.class);
//
//        binder().bind(IocResolver.class)
//                .to(GuiceIoCResolver.class);
//
//        configureResources();
//
//    }
//
//    /**
//     * Configures the resources for this {@link LuaResourceModule}.
//     */
//    protected abstract void configureResources();
//
//    /**
//     * Binds the given script file, returning a {@link ScriptFileBindingBuilder} which allows
//     * for the specification of further options.
//     *
//     * @param scriptFile the script file
//     * @return the {@link ScriptFileBindingBuilder}
//     */
//    protected BootstrapPathBindingBuilder bindEdgeScriptFile(final String scriptFile) {
//        return new BootstrapPathBindingBuilder() {
//            @Override
//            public ScriptFileBindingBuilder toBootstrapPath(final Path path) {
//                return scriptFileBindingBuilder(path, scriptFile);
//            }
//
//            @Override
//            public ScriptFileBindingBuilder toBootstrapPath(final String path) {
//                return toBootstrapPath(new Path(path));
//            }
//        };
//    }
//
//    private ScriptFileBindingBuilder scriptFileBindingBuilder(final Path bootstrapPath, final String scriptFile) {
//        return new ScriptFileBindingBuilder() {
//            @Override
//            public NamedScriptBindingBuilder fromClasspath() {
//                return edgeClasspathScriptFile(bootstrapPath, scriptFile);
//            }
//
//            @Override
//            public NamedScriptBindingBuilder fromLocalFilesystem() {
//                return edgeFilesystemScriptFile(bootstrapPath, new File(scriptFile));
//            }
//        };
//    }
//
//    private NamedScriptBindingBuilder edgeClasspathScriptFile(final Path bootstrapPath, final String scriptFile) {
//        return scriptName -> {
//
//            final Provider<LuaHandler> provider = EdgeResourceProviders.classpathProviderForScript(scriptFile);
//
//            return bootstrapEdgeResources.addBinding(bootstrapPath)
//                                         .toProvider(Providers.guicify(provider));
//
//        };
//    }
//
//    private NamedScriptBindingBuilder edgeFilesystemScriptFile(final Path bootstrapPath, final File scriptFile) {
//        return scriptName -> {
//
//            final Provider<LuaHandler> provider = EdgeResourceProviders
//                    .filesystemProviderForScript(scriptFile);
//
//            return bootstrapEdgeResources.addBinding(bootstrapPath)
//                                         .toProvider(Providers.guicify(provider));
//
//        };
//    }
//
//    /**
//     * Binds the given script file, returning a {@link ScriptFileBindingBuilder} which allows
//     * for the specification of further options.
//     *
//     * @param scriptFile the script file
//     * @return the {@link ScriptFileBindingBuilder}
//     */
//    protected ScriptFileBindingBuilder bindInternalScriptFile(final String scriptFile) {
//        return new ScriptFileBindingBuilder() {
//            @Override
//            public NamedScriptBindingBuilder fromClasspath() {
//                return internalClasspathScriptFile(scriptFile);
//            }
//
//            @Override
//            public NamedScriptBindingBuilder fromLocalFilesystem() {
//                return internalFilesystemScriptFile(new File(scriptFile));
//            }
//        };
//    }
//
//    private NamedScriptBindingBuilder internalClasspathScriptFile(final String scriptFile) {
//        return scriptName -> {
//
//            final Provider<LuaWorker> provider = classpathProviderForScript(scriptFile);
//
//            return bind(Worker.class)
//                    .annotatedWith(Names.named(scriptName))
//                    .toProvider(Providers.guicify(provider));
//
//        };
//    }
//
//    private NamedScriptBindingBuilder internalFilesystemScriptFile(final File scriptFile) {
//        return scriptName -> {
//
//            final Provider<LuaWorker> provider = filesystemProviderForScript(scriptFile);
//
//            return bind(Worker.class)
//                    .annotatedWith(Names.named(scriptName))
//                    .toProvider(Providers.guicify(provider));
//
//        };
//    }
//
//
//}
