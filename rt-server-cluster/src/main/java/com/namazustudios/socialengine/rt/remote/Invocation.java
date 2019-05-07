package com.namazustudios.socialengine.rt.remote;

import com.namazustudios.socialengine.rt.annotation.Dispatch;
import com.namazustudios.socialengine.rt.annotation.RoutingStrategy;

import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.List;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely.
 */
public class Invocation implements Serializable {

    private String type;

    private String name;

    private String method;

    private List<String> parameters;

    private List<Object> arguments;

    private Dispatch.Type dispatchType;

    private RoutingStrategy.Type routingStrategyType;

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
     * Gets a listing of the names of the method parameters.  Each parameter is named for the {@link Class<?>} it
     * represents.
     *
     * @return the {@link List<String>} of parameters
     */
    public List<String> getParameters() {
        return parameters;
    }

    /**
     * Sets a listing of the names of the method parameters.  Each parameter is named for the {@link Class<?>} it
     * represents.
     *
     * @param parameters the {@link List<String>} of parameters
     */
    public void setParameters(List<String> parameters) {
        this.parameters = parameters;
    }

    /**
     * Gets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @return a {@link List<Object>} containing the arguments to pass to the remote method
     */
    public List<Object> getArguments() {
        return arguments;
    }

    /**
     * Sets the arguments to pass to the remote method when invoking.  {@see {@link Method#invoke(Object, Object...)}}.
     *
     * @param arguments {@link List<Object>} containing the arguments to pass to the remote method
     */
    public void setArguments(List<Object> arguments) {
        this.arguments = arguments;
    }

    /**
     * Indicates the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the invocation
     * can be routed.
     *
     * @return the {@link Dispatch.Type}
     */
    public Dispatch.Type getDispatchType() {
        return dispatchType;
    }

    /**
     * Sets the {@link Dispatch.Type} used to send this invocation.  This can be used to hint how the invocation can be
     * routed.
     *
     * @return the {@link Dispatch.Type}
     */
    public void setDispatchType(Dispatch.Type dispatchType) {
        this.dispatchType = dispatchType;
    }

    public RoutingStrategy.Type getRoutingStrategyType() {
        return routingStrategyType;
    }

    public void setRoutingStrategyType(RoutingStrategy.Type routingStrategyType) {
        this.routingStrategyType = routingStrategyType;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Invocation)) return false;

        Invocation that = (Invocation) o;

        if (getType() != null ? !getType().equals(that.getType()) : that.getType() != null) return false;
        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getMethod() != null ? !getMethod().equals(that.getMethod()) : that.getMethod() != null) return false;
        if (getParameters() != null ? !getParameters().equals(that.getParameters()) : that.getParameters() != null)
            return false;
        if (getArguments() != null ? !getArguments().equals(that.getArguments()) : that.getArguments() != null)
            return false;
        if (getDispatchType() != that.getDispatchType()) {
            return false;
        }
        if (getRoutingStrategyType() != that.getRoutingStrategyType()) {
            return false;
        }
        return true;
    }

    @Override
    public int hashCode() {
        int result = getType() != null ? getType().hashCode() : 0;
        result = 31 * result + (getName() != null ? getName().hashCode() : 0);
        result = 31 * result + (getMethod() != null ? getMethod().hashCode() : 0);
        result = 31 * result + (getParameters() != null ? getParameters().hashCode() : 0);
        result = 31 * result + (getArguments() != null ? getArguments().hashCode() : 0);
        result = 31 * result + (getDispatchType() != null ? getDispatchType().hashCode() : 0);
        result = 31 * result + (getRoutingStrategyType() != null ? getRoutingStrategyType().hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "Invocation{" +
                "type='" + type + '\'' +
                ", name='" + name + '\'' +
                ", method='" + method + '\'' +
                ", parameters=" + parameters +
                ", arguments=" + arguments +
                ", dispatchType=" + dispatchType +
                ", routingStrategyType=" + routingStrategyType +
                '}';
    }
}
