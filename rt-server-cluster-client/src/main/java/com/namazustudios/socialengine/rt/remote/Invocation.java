package com.namazustudios.socialengine.rt.remote;

import java.util.List;

/**
 * Represents a remote invocation.  This contains enough information to invoke the method remotely.
 */
public class Invocation {

    private String type;

    private String name;

    private String method;

    private List<Object> parameters;

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public List<Object> getParameters() {
        return parameters;
    }

    public void setParameters(List<Object> parameters) {
        this.parameters = parameters;
    }

}
