package com.namazustudios.socialengine.rt.lua.guice;

/**
 * Used to specify the script's location, whether it be on disk or on the
 * classpath.  More complex logic can be added here if needed, such as specifying
 * the actual {@link ClassLoader} instance.
 *
 * Created by patricktwohig on 9/2/15.
 */
public interface ScriptFileBindingBuilder {

    /**
     * Uses the classpath to locate the actual script file.
     */
    NamedScriptBindingBuilder onClasspath();

    /**
     * Uses the local filesystem to locate the actual script file.
     */
    NamedScriptBindingBuilder onLocalFilesystem();

}
