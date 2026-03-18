package dev.getelements.elements.sdk.model.mission;

import dev.getelements.elements.sdk.model.ValidWithGroups;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Read;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import io.swagger.v3.oas.annotations.media.Schema;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Null;
import java.util.List;
import java.util.Objects;

/** Represents a time-bounded event within a schedule, associating missions with a time range. */
@Schema
public class ScheduleEvent {

    /** Creates a new instance. */
    public ScheduleEvent() {}

    @Null(groups = Insert.class)
    @NotNull(groups = {Update.class, Read.class})
    private String id;

    @Min(0)
    private Long begin;

    @Min(0)
    private Long end;

    @NotNull
    @ValidWithGroups(Read.class)
    private Schedule schedule;

    @NotNull
    private List<@ValidWithGroups(Read.class) Mission> missions;

    /**
     * Returns the unique ID of this schedule event.
     *
     * @return the id
     */
    public String getId() {
        return id;
    }

    /**
     * Sets the unique ID of this schedule event.
     *
     * @param id the id
     */
    public void setId(String id) {
        this.id = id;
    }

    /**
     * Returns the begin time of this event in milliseconds since Unix epoch.
     *
     * @return the begin time
     */
    public Long getBegin() {
        return begin;
    }

    /**
     * Sets the begin time of this event in milliseconds since Unix epoch.
     *
     * @param begin the begin time
     */
    public void setBegin(Long begin) {
        this.begin = begin;
    }

    /**
     * Returns the end time of this event in milliseconds since Unix epoch.
     *
     * @return the end time
     */
    public Long getEnd() {
        return end;
    }

    /**
     * Sets the end time of this event in milliseconds since Unix epoch.
     *
     * @param end the end time
     */
    public void setEnd(Long end) {
        this.end = end;
    }

    /**
     * Returns the schedule this event belongs to.
     *
     * @return the schedule
     */
    public Schedule getSchedule() {
        return schedule;
    }

    /**
     * Sets the schedule this event belongs to.
     *
     * @param schedule the schedule
     */
    public void setSchedule(Schedule schedule) {
        this.schedule = schedule;
    }

    /**
     * Returns the missions associated with this schedule event.
     *
     * @return the missions
     */
    public List<Mission> getMissions() {
        return missions;
    }

    /**
     * Sets the missions associated with this schedule event.
     *
     * @param missions the missions
     */
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
