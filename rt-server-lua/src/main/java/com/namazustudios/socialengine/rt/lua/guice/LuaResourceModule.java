package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.AbstractModule;
import com.google.inject.binder.ScopedBindingBuilder;

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
                return edgeFilesystemScriptFile(scriptFile);
            }
        };
    }

    private NamedScriptBindingBuilder edgeClasspathScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {
                return null;

            }
        };
    }

    private NamedScriptBindingBuilder edgeFilesystemScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(String scriptName) {
                return null;
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
                return internalFilesystemScriptFile(scriptFile);
            }
        };
    }

    private NamedScriptBindingBuilder internalClasspathScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(final String scriptName) {
                return null;

            }
        };
    }

    private NamedScriptBindingBuilder internalFilesystemScriptFile(final String scriptFile) {
        return new NamedScriptBindingBuilder() {
            @Override
            public ScopedBindingBuilder named(String scriptName) {
                return null;
            }
        };
    }

}
