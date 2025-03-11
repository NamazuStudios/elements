package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.ValidationGroups;
import io.swagger.v3.oas.annotations.media.Schema;


import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import jakarta.validation.constraints.Pattern;
import java.util.Objects;

import static dev.getelements.elements.sdk.model.Constants.Regexp.WHOLE_WORD_ONLY;

@Schema
public class Schedule {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @Schema(description = "The unique ID of the schedule.")
    private String id;

    @NotNull
    @Pattern(regexp = WHOLE_WORD_ONLY)
    @Schema(description = "The unique name of the schedule.")
    private String name;

    @NotNull
    @Schema
    private String displayName;

    @NotNull
    @Schema(description = "The description for this schedule.")
    private String description;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Schedule schedule = (Schedule) o;
        return Objects.equals(getId(), schedule.getId()) && Objects.equals(getName(), schedule.getName()) && Objects.equals(getDisplayName(), schedule.getDisplayName()) && Objects.equals(getDescription(), schedule.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDisplayName(), getDescription());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Schedule{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", displayName='").append(displayName).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
