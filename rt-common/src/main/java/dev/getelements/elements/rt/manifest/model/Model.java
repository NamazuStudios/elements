package dev.getelements.elements.rt.manifest.model;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.util.Map;
import java.util.Objects;

/**
 * Created by patricktwohig on 8/16/17.
 */
public class Model implements Serializable {

    @NotNull
    private String name;

    @NotNull
    private String description;

    @Valid
    @NotNull
    private Map<@NotNull String, @NotNull Property> properties;

    /**
     * Get the name of the model.
     *
     * @return the name of the model
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the name of the model.
     *
     * @param name the name of the model
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the description of this {@link Model}.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of this model.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the mapping of properties.
     *
     * @return the {@link Map<String, Property>}
     */
    public Map<String, Property> getProperties() {
        return properties;
    }

    /**
     * Sets the mapping of properties.
     *
     * @param properties the {@link Map<String, Property>}
     */
    public void setProperties(Map<String, Property> properties) {
        this.properties = properties;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Model model = (Model) o;
        return Objects.equals(getName(), model.getName()) && Objects.equals(getDescription(), model.getDescription()) && Objects.equals(getProperties(), model.getProperties());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getProperties());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Model{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", properties=").append(properties);
        sb.append('}');
        return sb.toString();
    }

}
