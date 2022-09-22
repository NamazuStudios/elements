package com.namazustudios.socialengine.rt.jrpc;

import java.io.Serializable;
import java.util.Objects;

public class JsonRpcError implements Serializable {

    public static final String V_2_0 = JsonRpcRequest.V_2_0;

    private String jsonrpc;

    private String message;

    private Object data;

    public String getJsonrpc() {
        return jsonrpc;
    }

    public void setJsonrpc(String jsonrpc) {
        this.jsonrpc = jsonrpc;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Object getData() {
        return data;
    }

    public void setData(Object data) {
        this.data = data;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcError jsonRpcError = (JsonRpcError) o;
        return Objects.equals(getJsonrpc(), jsonRpcError.getJsonrpc()) && Objects.equals(getMessage(), jsonRpcError.getMessage()) && Objects.equals(getData(), jsonRpcError.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJsonrpc(), getMessage(), getData());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JrpcError{");
        sb.append("jsonrpc='").append(jsonrpc).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append('}');
        return sb.toString();
    }

}
