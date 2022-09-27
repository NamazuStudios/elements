package com.namazustudios.socialengine.rt.manifest.http;

import com.namazustudios.socialengine.rt.manifest.model.Type;

import javax.validation.constraints.NotNull;
import java.io.Serializable;

/**
 * Stores metadata for a specific HTTP request,
 */
public class Header implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private Type type;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
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
        if (!(o instanceof Header)) return false;

        Header that = (Header) o;

        if (getName() != null ? !getName().equals(that.getName()) : that.getName() != null) return false;
        if (getDescription() != null ? !getDescription().equals(that.getDescription()) : that.getDescription() != null)
            return false;
        return getType() == that.getType();
    }

    @Override
    public int hashCode() {
        int result = getName() != null ? getName().hashCode() : 0;
        result = 31 * result + (getDescription() != null ? getDescription().hashCode() : 0);
        result = 31 * result + (getType() != null ? getType().hashCode() : 0);
        return result;
    }

}
