package com.namazustudios.socialengine.rt.remote;

import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely.
 */
public class Invocation {

    private String type;

    private String name;

    private String method;

    private List<Object> parameters;

    /**
     * Gets the string representing the type of the remote object to invoke.  {@see {@link Class#getName()}}
     *
     * @return the the type
     */
    public String getType() {
        return type;
    }

    /**
     * Sets the string representing the type of the remote object to invoke.  {@see {@link Class#getName()}}
     *
     * @param type the the type
     */
    public void setType(String type) {
        this.type = type;
    }

    /**
     * Gets the name of the remote object to invoke.  {@see {@link javax.inject.Named}}.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the remote object to invoke.  {@see {@link javax.inject.Named}}.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the name of the remote method to invoke.  {@see {@link Method#getName()}}.
     *
     * @return the name
     */
    public String getMethod() {
        return method;
    }

    /**
     * Sets the name of the remote method to invoke.  {@see {@link Method#getName()}}.
     *
     * @param method the method name
     */
    public void setMethod(String method) {
        this.method = method;
    }

    /**
     * Gets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @return a {@link List<Object>} containing the parameters to pass to the remote method
     */
    public List<Object> getParameters() {
        return parameters;
    }

    /**
     * Sets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @param parameters {@link List<Object>} containing the parameters to pass to the remote method
     */
    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

}
