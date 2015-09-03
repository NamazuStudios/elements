package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.binder.ScopedBindingBuilder;
import com.google.inject.name.Names;
import com.google.inject.util.Providers;
import com.namazustudios.socialengine.rt.edge.EdgeResource;
import com.namazustudios.socialengine.rt.internal.InternalResource;
import com.namazustudios.socialengine.rt.lua.LuaEdgeResource;
import com.namazustudios.socialengine.rt.lua.EdgeResourceProviders;
import com.namazustudios.socialengine.rt.lua.LuaInternalResource;
import com.namazustudios.socialengine.rt.lua.InternalResourceProviders;

import javax.inject.Provider;
import java.io.File;


/**
 * Allows for client code to manually bind Lua scripts with names.  Once bound, they can
 * be accessed through the {@link GuiceIoCResolver}.
 *
 * Created by patricktwohig on 9/1/15.
 */
public abstract class LuaResourceModule extends AbstractModule {

    /**
     * Binds the given script file, returning a {@link ScriptFileBindingBuilder} which allows
     * for the specification of further options.
     *
     * @param scriptFile the script file
     * @return the {@link ScriptFileBindingBuilder}
     */
    protected ScriptFileBindingBuilder bindEdgeScriptFile(final String scriptFile) {
        return new ScriptFileBindingBuilder() {
            @Override
            public NamedScriptBindingBuilder onClasspath() {
                return edgeClasspathScriptFile(scriptFile);
            }

            @Override
            public NamedScriptBindingBuilder onLocalFilesystem() {
                return edgeFilesystemScriptFile(new File(scriptFile));
            }
        };
    }

    private NamedScriptBindingBuilder edgeClasspathScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {

                final Provider<LuaEdgeResource> provider = EdgeResourceProviders
                        .classpathProviderForScript(scriptFile);

                return bind(EdgeResource.class)
                        .annotatedWith(Names.named(scriptName))
                        .toProvider(Providers.guicify(provider));

            }
        };
    }

    private NamedScriptBindingBuilder edgeFilesystemScriptFile(final File scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {

                final Provider<LuaEdgeResource> provider = EdgeResourceProviders
                        .filesystemProviderForScript(scriptFile);

                return bind(EdgeResource.class)
                        .annotatedWith(Names.named(scriptName))
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
