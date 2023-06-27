package dev.getelements.elements.rt.manifest.model;

import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Objects;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class Property implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @NotNull
    private Type type;

    @NotNull
    private String model;

    /**
     * Gets the name of this property.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of this Property.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this {@link Property}.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this {@link Property}.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * The type of this property.
     *
     * @return the type.
     */
    public Type getType() {
        return type;
    }

    /**
     * Sets the type of this property.
     *
     * @param type the type
     */
    public void setType(Type type) {
        this.type = type;
    }

    /**
     * Gets the model name, if this is a complex type. (eg {@link Type#ARRAY} or {@link Type#OBJECT})
     * such that it may property reference another model in the manifest.  THis corresponds to
     * the values of {@link Model#getName()} and the key in {@link ModelManifest#getModelsByName()}.
     *
     * @return
     */
    public String getModel() {
        return model;
    }

    /**
     * Sets the model name.
     *
     * @param model the model name.
     */
    public void setModel(String model) {
        this.model = model;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Property property = (Property) o;
        return Objects.equals(getName(), property.getName()) && Objects.equals(getDescription(), property.getDescription()) && getType() == property.getType() && Objects.equals(getModel(), property.getModel());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getType(), getModel());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Property{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", type=").append(type);
        sb.append(", model='").append(model).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
