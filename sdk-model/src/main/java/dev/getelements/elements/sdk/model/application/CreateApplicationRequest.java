package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.io.Serializable;
import java.util.Map;

/** Represents the request body for creating a new application. */
@Schema
public class CreateApplicationRequest implements Serializable {

    /** Creates a new instance. */
    public CreateApplicationRequest() {}

    private String id;

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    private String description;

    private Map<String, Object> attributes;

    /**
     * Returns the application description.
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the application description.
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Returns the unique application name.
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique application name.
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the application attributes.
     * @return the attributes
     */
    public Map<String, Object> getAttributes() {
        return attributes;
    }

    /**
     * Sets the application attributes.
     * @param attributes the attributes
     */
    public void setAttributes(Map<String, Object> attributes) {
        this.attributes = attributes;
    }
}
