package dev.getelements.elements.rt.manifest.jrpc;

import dev.getelements.elements.rt.manifest.Deprecation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JsonRpcService implements Serializable {

    @NotNull
    private String scope;

    @NotNull
    private Deprecation deprecation;

    @Valid
    @NotNull
    private List<JsonRpcMethod> jsonRpcMethodList;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public Deprecation getDeprecation() {
        return deprecation;
    }

    public void setDeprecation(Deprecation deprecation) {
        this.deprecation = deprecation;
    }

    public List<JsonRpcMethod> getJsonRpcMethodList() {
        return jsonRpcMethodList;
    }

    public void setJsonRpcMethodList(List<JsonRpcMethod> jsonRpcMethodList) {
        this.jsonRpcMethodList = jsonRpcMethodList;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcService that = (JsonRpcService) o;
        return Objects.equals(getScope(), that.getScope()) && Objects.equals(getDeprecation(), that.getDeprecation()) && Objects.equals(getJsonRpcMethodList(), that.getJsonRpcMethodList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScope(), getDeprecation(), getJsonRpcMethodList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcService{");
        sb.append("scope='").append(scope).append('\'');
        sb.append(", definition=").append(deprecation);
        sb.append(", jsonRpcMethodList=").append(jsonRpcMethodList);
        sb.append('}');
        return sb.toString();
    }

}
