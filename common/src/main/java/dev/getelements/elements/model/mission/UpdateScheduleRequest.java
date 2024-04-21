package dev.getelements.elements.model.mission;

import dev.getelements.elements.Constants;
import io.swagger.annotations.ApiModel;

import javax.validation.constraints.NotNull;
import javax.validation.constraints.Pattern;
import java.util.Objects;

@ApiModel
public class UpdateScheduleRequest {

    @NotNull
    @Pattern(regexp = Constants.Regexp.WHOLE_WORD_ONLY)
    private String name;

    @NotNull
    private String displayName;

    @NotNull
    private String description;

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
