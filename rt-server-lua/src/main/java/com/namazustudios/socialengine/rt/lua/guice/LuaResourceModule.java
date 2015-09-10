package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Scopes;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.multibindings.MapBinder;
import com.google.inject.multibindings.Multibinder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.Path;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.edge.EdgeServer;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.lua.*;

import javax.inject.Provider;
import java.io.File;


/**
 * Allows for client code to manually bind Lua scripts with names.  Once bound, they can
 * be accessed through the {@link GuiceIoCResolver}.
 *
 * Created by patricktwohig on 9/1/15.
 */
public abstract class LuaResourceModule extends AbstractModule {

    private MapBinder<Path, EdgeResource> bootstrapEdgeResources;

    @Override
    protected final void configure() {

        bootstrapEdgeResources = MapBinder.newMapBinder(binder(),
                                                        Path.class,
                                                        EdgeResource.class,
                                                        Names.named(EdgeServer.BOOTSTRAP_RESOURCES));

        binder().bind(IocResolver.class).to(GuiceIoCResolver.class).in(Scopes.SINGLETON);

        configureResources();

    }

    /**
     * Configures the resources for this {@link LuaResourceModule}.
     */
    protected abstract void configureResources();

    /**
     * Binds the given script file, returning a {@link ScriptFileBindingBuilder} which allows
     * for the specification of further options.
     *
     * @param scriptFile the script file
     * @return the {@link ScriptFileBindingBuilder}
     */
    protected BootstrapPathBindingBuilder bindEdgeScriptFile(final String scriptFile) {
        return new BootstrapPathBindingBuilder() {
            @Override
            public ScriptFileBindingBuilder onBootstrapPath(final Path path) {
                return scriptFileBindingBuilder(path, scriptFile);
            }

            @Override
            public ScriptFileBindingBuilder onBootstrapPath(final String path) {
                return onBootstrapPath(new Path(path));
            }
        };
    }

    private ScriptFileBindingBuilder scriptFileBindingBuilder(final Path bootstrapPath, final String scriptFile) {
        return new ScriptFileBindingBuilder() {
            @Override
            public NamedScriptBindingBuilder onClasspath() {
                return edgeClasspathScriptFile(bootstrapPath, scriptFile);
            }

            @Override
            public NamedScriptBindingBuilder onLocalFilesystem() {
                return edgeFilesystemScriptFile(bootstrapPath, new File(scriptFile));
            }
        };
    }

    private NamedScriptBindingBuilder edgeClasspathScriptFile(final Path bootstrapPath, final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {

                final Provider<LuaEdgeResource> provider = EdgeResourceProviders
                        .classpathProviderForScript(scriptFile);

                return bootstrapEdgeResources.addBinding(bootstrapPath)
                                             .toProvider(Providers.guicify(provider));

            }
        };
    }

    private NamedScriptBindingBuilder edgeFilesystemScriptFile(final Path bootstrapPath, final File scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {

                final Provider<LuaEdgeResource> provider = EdgeResourceProviders
                        .filesystemProviderForScript(scriptFile);

                return bootstrapEdgeResources.addBinding(bootstrapPath)
                                             .toProvider(Providers.guicify(provider));

            }
        };
    }

    /**
     * Binds the given script file, returning a {@link ScriptFileBindingBuilder} which allows
     * for the specification of further options.
     *
     * @param scriptFile the script file
     * @return the {@link ScriptFileBindingBuilder}
     */
    protected ScriptFileBindingBuilder bindInternalScriptFile(final String scriptFile) {
        return new ScriptFileBindingBuilder() {
            @Override
            public NamedScriptBindingBuilder onClasspath() {
                return internalClasspathScriptFile(scriptFile);
            }

            @Override
            public NamedScriptBindingBuilder onLocalFilesystem() {
                return internalFilesystemScriptFile(new File(scriptFile));
            }
        };
    }

    private NamedScriptBindingBuilder internalClasspathScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {

                final Provider<LuaInternalResource> provider = InternalResourceProviders
                        .classpathProviderForScript(scriptFile);

                return bind(InternalResource.class)
                        .annotatedWith(Names.named(scriptName))
                        .toProvider(Providers.guicify(provider));

            }
        };
    }

    private NamedScriptBindingBuilder internalFilesystemScriptFile(final File scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(String scriptName) {

                final Provider<LuaInternalResource> provider = InternalResourceProviders
                        .filesystemProviderForScript(scriptFile);

                return bind(InternalResource.class)
                        .annotatedWith(Names.named(scriptName))
                        .toProvider(Providers.guicify(provider));

            }
        };
    }


}
