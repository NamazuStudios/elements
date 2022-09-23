package com.namazustudios.socialengine.rt.manifest.jrpc;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JsonRpcMethod implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private List<@NotNull JsonRpcParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<JsonRpcParameter> getParameters() {
        return parameters;
    }

    public void setParameters(List<JsonRpcParameter> parameters) {
        this.parameters = parameters;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcMethod that = (JsonRpcMethod) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getParameters());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcMethod{");
        sb.append("name='").append(name).append('\'');
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }

}
