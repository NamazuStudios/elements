package dev.getelements.elements.model.mission;

import dev.getelements.elements.model.ValidationGroups;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import javax.validation.constraints.Pattern;
import java.util.Objects;

import static dev.getelements.elements.Constants.Regexp.WHOLE_WORD_ONLY;

@ApiModel
public class Schedule {

    @Null(groups = ValidationGroups.Insert.class)
    @NotNull(groups = ValidationGroups.Update.class)
    @ApiModelProperty("The unique ID of the schedule.")
    private String id;

    @NotNull
    @Pattern(regexp = WHOLE_WORD_ONLY)
    @ApiModelProperty("The unique name of the schedule.")
    private String name;

    @NotNull
    @ApiModelProperty("The description for this schedule.")
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
        return Objects.equals(getId(), schedule.getId()) && Objects.equals(getName(), schedule.getName()) && Objects.equals(getDescription(), schedule.getDescription());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getName(), getDescription());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Schedule{");
        sb.append("id='").append(id).append('\'');
        sb.append(", name='").append(name).append('\'');
        sb.append(", description='").append(description).append('\'');
        sb.append('}');
        return sb.toString();
    }

}
