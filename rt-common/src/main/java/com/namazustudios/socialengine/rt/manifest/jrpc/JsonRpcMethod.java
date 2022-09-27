package com.namazustudios.socialengine.rt.manifest.jrpc;

import com.namazustudios.socialengine.rt.manifest.Deprecation;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JsonRpcMethod implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private Deprecation deprecation;

    @NotNull
    private List<@NotNull JsonRpcParameter> parameters;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Deprecation getDeprecation() {
        return deprecation;
    }

    public void setDeprecation(Deprecation deprecation) {
        this.deprecation = deprecation;
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
        return Objects.equals(getName(), that.getName()) && Objects.equals(getDeprecation(), that.getDeprecation()) && Objects.equals(getParameters(), that.getParameters());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDeprecation(), getParameters());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcMethod{");
        sb.append("name='").append(name).append('\'');
        sb.append(", deprecation=").append(deprecation);
        sb.append(", parameters=").append(parameters);
        sb.append('}');
        return sb.toString();
    }

}
