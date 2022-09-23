package com.namazustudios.socialengine.rt.manifest.jrpc;

import com.namazustudios.socialengine.rt.manifest.model.Type;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

public class JsonRpcParameter implements Serializable {

    @Min(0)
    @NotNull
    private int index;

    @NotNull
    private Type type;

    public int getIndex() {
        return index;
    }

    public void setIndex(int index) {
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public void setType(Type type) {
        this.type = type;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcParameter that = (JsonRpcParameter) o;
        return getIndex() == that.getIndex() && getType() == that.getType();
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getType());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcParameter{");
        sb.append("index=").append(index);
        sb.append(", type=").append(type);
        sb.append('}');
        return sb.toString();
    }

}
