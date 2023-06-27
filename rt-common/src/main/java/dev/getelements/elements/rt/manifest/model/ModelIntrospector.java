package dev.getelements.elements.rt.manifest.model;

import dev.getelements.elements.rt.annotation.CodeStyle;
import dev.getelements.elements.rt.annotation.RemoteScope;

/**
 * Introspects models based on the underlying serialization configuration.
 */
public interface ModelIntrospector {

    /**
     * Introspects the supplied {@link Class<?>} for the supplied model.
     * @param cls the class
     * @return the {@link Type}
     */
    Type introspectClassForType(Class<?> cls);

    /**
     * Introspects the given {@link Class<?>}, converting the types using the supplied {@link CodeStyle}.
     *
     * @param cls the {@link Class<?>} to introspect
     * @param remoteScope the remote scope
     * @return the introspection result
     */
    Model introspectClassForModel(Class<?> cls, RemoteScope remoteScope);

    /**
     * Introspects the class for the model name, but does not generate the whole model.
     * @param cls the {@link Class<?>} to introspect
     * @param remoteScope the remote scope
     * @return the model name
     */
    String introspectClassForModelName(Class<?> cls, RemoteScope remoteScope);

}
