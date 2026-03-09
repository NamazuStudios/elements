package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.Constants;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

/** Represents a request to create a mission schedule. */
@Schema
public class CreateScheduleRequest {

    /** Creates a new instance. */
    public CreateScheduleRequest() {}

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    private String displayName;

    @NotNull
    private String description;

    /**
     * Returns the unique name of the schedule.
     *
     * @return the name
     */
    public String getName() {
        return name;
    }

    /**
     * Sets the unique name of the schedule.
     *
     * @param name the name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Returns the display name of the schedule.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return displayName;
    }

    /**
     * Sets the display name of the schedule.
     *
     * @param displayName the display name
     */
    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    /**
     * Returns the description of the schedule.
     *
     * @return the description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Sets the description of the schedule.
     *
     * @param description the description
     */
    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateScheduleRequest that = (CreateScheduleRequest) o;
        return Objects.equals(getName(), that.getName()) &&
                Objects.equals(getDescription(), that.getDescription()) &&
                Objects.equals(getDisplayName(), that.getDisplayName());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getName(), getDescription(), getDisplayName());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateScheduleRequest{");
        sb.append("name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
