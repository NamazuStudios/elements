package dev.getelements.elements.model.mission;

import dev.getelements.elements.model.ValidationGroups;
import dev.getelements.elements.model.ValidationGroups.Insert;
import dev.getelements.elements.model.ValidationGroups.Read;
import dev.getelements.elements.model.ValidationGroups.Update;
import io.swagger.annotations.ApiModel;

import javax.validation.Valid;
import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Null;
import java.util.List;
import java.util.Objects;

@ApiModel
public class ScheduleEvent {

    @Null(groups = Insert.class)
    @NotNull(groups = {Update.class, Read.class})
    private String id;

    @Min(0)
    private Long begin;

    @Min(0)
    private Long end;

    @NotNull
    private Schedule schedule;

    @NotNull
    private List<Mission> missions;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public Long getBegin() {
        return begin;
    }

    public void setBegin(Long begin) {
        this.begin = begin;
    }

    public Long getEnd() {
        return end;
    }

    public void setEnd(Long end) {
        this.end = end;
    }

    public Schedule getSchedule() {
        return schedule;
    }

    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    public List<Mission> getMissions() {
        return missions;
    }

    public void setMissions(List<Mission> missions) {
        this.missions = missions;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEvent that = (ScheduleEvent) o;
        return Objects.equals(getId(), that.getId()) && Objects.equals(getBegin(), that.getBegin()) && Objects.equals(getEnd(), that.getEnd()) && Objects.equals(getSchedule(), that.getSchedule()) && Objects.equals(getMissions(), that.getMissions());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBegin(), getEnd(), getSchedule(), getMissions());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("ScheduleEvent{");
        sb.append("id='").append(id).append('\'');
        sb.append(", begin=").append(begin);
        sb.append(", end=").append(end);
        sb.append(", schedule=").append(schedule);
        sb.append('}');
        return sb.toString();
    }

}
