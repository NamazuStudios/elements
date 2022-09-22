package com.namazustudios.socialengine.rt.jrpc;

import java.io.Serializable;
import java.util.Objects;

public class JsonRpcResponse implements Serializable {

    public static final String V_2_0 = JsonRpcRequest.V_2_0;

    private String jsonrpc;

    private String result;

    private String error;

    private String id;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getResult() {
        return result;
    }

    public void setResult(String result) {
        this.result = result;
    }

    public String getError() {
        return error;
    }

    public void setError(String error) {
        this.error = error;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcResponse that = (JsonRpcResponse) o;
        return Objects.equals(getJsonrpc(), that.getJsonrpc()) && Objects.equals(getResult(), that.getResult()) && Objects.equals(getError(), that.getError()) && Objects.equals(getId(), that.getId());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJsonrpc(), getResult(), getError(), getId());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcResponse{");
        sb.append("jsonrpc='").append(jsonrpc).append('\'');
        sb.append(", result='").append(result).append('\'');
        sb.append(", error='").append(error).append('\'');
        sb.append(", id='").append(id).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
