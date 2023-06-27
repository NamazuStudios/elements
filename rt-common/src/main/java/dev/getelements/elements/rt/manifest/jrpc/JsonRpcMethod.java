package dev.getelements.elements.rt.manifest.jrpc;

import dev.getelements.elements.rt.manifest.Deprecation;
import dev.getelements.elements.rt.manifest.model.Type;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JsonRpcMethod implements Serializable {

    @NotNull
    private String name;

    @Valid
    @NotNull
    private Deprecation deprecation;

    @Valid
    @NotNull
    private List<@NotNull JsonRpcParameter> parameters;

    @Valid
    private JsonRpcReturnType returns;

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

    public JsonRpcReturnType getReturns() {
        return returns;
    }

    public void setReturns(JsonRpcReturnType returns) {
        this.returns = returns;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcMethod that = (JsonRpcMethod) o;
        return Objects.equals(getName(), that.getName()) && Objects.equals(getDeprecation(), that.getDeprecation()) && Objects.equals(getParameters(), that.getParameters()) && Objects.equals(getReturns(), that.getReturns());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDeprecation(), getParameters(), getReturns());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcMethod{");
        sb.append("name='").append(name).append('\'');
        sb.append(", deprecation=").append(deprecation);
        sb.append(", parameters=").append(parameters);
        sb.append(", returns=").append(returns);
        sb.append('}');
        return sb.toString();
    }

}
