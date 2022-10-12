package com.namazustudios.socialengine.rt.jrpc;

import java.io.Serializable;
import java.util.Objects;

public class JsonRpcRequest implements Serializable {

    public static final String V_2_0 = "2.0";

    private String jsonrpc = V_2_0;

    private String method;

    private Object params;

    private Object id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public Object getParams() {
        return params;
    }

    public void setParams(Object params) {
        this.params = params;
    }

    public Object getId() {
        return id;
    }

    public void setId(Object id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcRequest that = (JsonRpcRequest) o;
        return Objects.equals(getJsonrpc(), that.getJsonrpc()) && Objects.equals(getMethod(), that.getMethod()) && Objects.equals(getParams(), that.getParams()) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJsonrpc(), getMethod(), getParams(), getId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcRequest{");
        sb.append("jsonrpc='").append(jsonrpc).append('\'');
        sb.append(", method='").append(method).append('\'');
        sb.append(", params='").append(params).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
