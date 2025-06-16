package dev.getelements.elements.sdk.model.application;

import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;

import java.io.Serializable;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.WHOLE_WORD_ONLY;

/**
 * Ties the {@link Application} model to one of its associated profiles as represented by the type. The type indicates
 * the fully qualified class name of the model that represents the profile. For example, a matchmaking profile is
 * {@link MatchmakingApplicationConfiguration}
 *
 * Created by patricktwohig on 7/10/15.
 */
@Schema
public class ApplicationConfiguration implements Serializable {

    @Schema(description = "The database assigned ID for the application configuration.")
    @Null(groups = Insert.class)
    @NotNull(groups = Update.class)
    private String id;

    @NotNull
    @Pattern(regexp = WHOLE_WORD_ONLY)
    @Schema(description = "The application-configuration specific unique ID. Unique per application per category.")
    private String name;

    @NotNull
    @Schema(description = "The fully-qualified Java type of ApplicationConfiguration.")
    private String type = getClass().getName();

    @NotNull
    private String description;

    @Valid
    @NotNull
    @Schema(description = "The parent application owning this configuration.")
    private Application parent;

    /**
     * Gets the actual profile ID.
     *
     * @return the profile ID
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the actual profile ID.
     *
     * @param id the profile ID
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Gets the unique identifier for the category.
     *
     * @return
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique identifier for the category.
     *
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    /**
     * Gets the parent {@link Application}
     *
     * @return the parent
     */
    public Application getParent() {
        return parent;
    }

    /**
     * Sets the parent {@link Application}
     *
     * @param parent the parent
     */
    public void setParent(Application parent) {
        this.parent = parent;
    }

    @Override
    public boolean equals(Object object) {
        if (object == null || getClass() != object.getClass()) return false;
        ApplicationConfiguration that = (ApplicationConfiguration) object;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getType(), that.getType()) && Objects.equals(getDescription(), that.getDescription()) && Objects.equals(getParent(), that.getParent());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getType(), getDescription(), getParent());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ApplicationConfiguration{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", type='").append(type).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", parent=").append(parent);
        sb.append('}');
        return sb.toString();
    }

}
