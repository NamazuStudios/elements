package com.namazustudios.socialengine.rt.jrpc;

import java.io.Serializable;
import java.util.Objects;

public class JsonRpcError implements Serializable {

    public static final String V_2_0 = JsonRpcRequest.V_2_0;

    public static final int PARSE_ERROR = -32700;

    public static final int INVALID_REQUEST = -32600;

    public static final int METHOD_NOT_FOUND = -32601;

    public static final int INVALID_PARAMETERS = -32602;

    public static final int INTERNAL_ERROR = -32603;

    public static final int SERVER_ERROR_MIN = -32099;

    public static final int SERVER_ERROR_MAX = -32000;

    private String jsonrpc = V_2_0;

    private String message;

    private Object data;

    private int code;

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

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcError that = (JsonRpcError) o;
        return getCode() == that.getCode() && Objects.equals(getJsonrpc(), that.getJsonrpc()) && Objects.equals(getMessage(), that.getMessage()) && Objects.equals(getData(), that.getData());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getJsonrpc(), getMessage(), getData(), getCode());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcError{");
        sb.append("jsonrpc='").append(jsonrpc).append('\'');
        sb.append(", message='").append(message).append('\'');
        sb.append(", data=").append(data);
        sb.append(", code=").append(code);
        sb.append('}');
        return sb.toString();
    }

}
