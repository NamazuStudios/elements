package com.namazustudios.socialengine.rt.manifest.jrpc;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.List;
import java.util.Objects;

public class JsonRpcService implements Serializable {

    @NotNull
    private String scope;

    @NotNull
    private boolean deprecated;

    @NotNull
    private String deprecationMessage;

    @NotNull
    private List<JsonRpcMethod> jsonRpcMethodList;

    public String getScope() {
        return scope;
    }

    public void setScope(String scope) {
        this.scope = scope;
    }

    public boolean isDeprecated() {
        return deprecated;
    }

    public void setDeprecated(boolean deprecated) {
        this.deprecated = deprecated;
    }

    public String getDeprecationMessage() {
        return deprecationMessage;
    }

    public void setDeprecationMessage(String deprecationMessage) {
        this.deprecationMessage = deprecationMessage;
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
        return isDeprecated() == that.isDeprecated() && Objects.equals(getScope(), that.getScope()) && Objects.equals(getDeprecationMessage(), that.getDeprecationMessage()) && Objects.equals(getJsonRpcMethodList(), that.getJsonRpcMethodList());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getScope(), isDeprecated(), getDeprecationMessage(), getJsonRpcMethodList());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcService{");
        sb.append("scope='").append(scope).append('\'');
        sb.append(", deprecated=").append(deprecated);
        sb.append(", deprecationMessage='").append(deprecationMessage).append('\'');
        sb.append(", jsonRpcMethodList=").append(jsonRpcMethodList);
        sb.append('}');
        return sb.toString();
    }
}
