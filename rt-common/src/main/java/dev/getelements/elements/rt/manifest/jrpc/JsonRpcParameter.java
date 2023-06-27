package dev.getelements.elements.rt.manifest.jrpc;

import dev.getelements.elements.rt.manifest.model.Type;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

public class JsonRpcParameter implements Serializable {

    @Min(0)
    private Integer index;

    @NotNull
    private Type type;

    private String model;

    private String name;

    public Integer getIndex() {
        return index;
    }

    public void setIndex(Integer index) {
        this.index = index;
    }

    public Type getType() {
        return type;
    }

    public void setType(final Type type) {
        this.type = type;
    }

    public String getModel() {
        return model;
    }

    public void setModel(String model) {
        this.model = model;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        JsonRpcParameter that = (JsonRpcParameter) o;
        return Objects.equals(getIndex(), that.getIndex()) && getType() == that.getType() && Objects.equals(getModel(), that.getModel()) && Objects.equals(getName(), that.getName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getIndex(), getType(), getModel(), getName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("JsonRpcParameter{");
        sb.append("index=").append(index);
        sb.append(", type=").append(type);
        sb.append(", model='").append(model).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
