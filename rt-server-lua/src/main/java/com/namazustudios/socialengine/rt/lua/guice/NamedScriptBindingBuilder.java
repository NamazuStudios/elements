package com.namazustudios.socialengine.rt.lua.guice;

import com.google.inject.binder.ScopedBindingBuilder;

/**
 * Used to specify the name of the script.  The script will be bound to it's matching
 * {@link com.namazustudios.socialengine.rt.Resource} type and differentiated from
 * other scripts using the {@link javax.inject.Named} annotation.
 *
 * Created by patricktwohig on 9/2/15.
 */
public interface NamedScriptBindingBuilder {

    /**
     * Specifies the name of the script, returning an instance of {@link ScopedBindingBuilder}.
     *
     * @param scriptName the name of the script.
     *
     * @return a {@link ScopedBindingBuilder} allowing for the specification of the script's scope.
     */
    ScopedBindingBuilder named(String scriptName);

}
