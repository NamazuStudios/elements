package com.namazustudios.socialengine.rt.manifest.jrpc;

import com.namazustudios.socialengine.rt.manifest.model.Type;

import javax.validation.constraints.NotNull;
import java.util.Objects;

public class JsonRpcReturnType {

    @NotNull
    private Type type;

    private String model;

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcReturnType that = (JsonRpcReturnType) o;
        return getType() == that.getType() && Objects.equals(getModel(), that.getModel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getType(), getModel());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcReturnType{");
        sb.append("type=").append(type);
        sb.append(", model='").append(model).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
