package dev.getelements.elements.sdk.model.mission;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

/** Represents a request to update a schedule event's time range and associated missions. */
public class UpdateScheduleEventRequest {

    /** Creates a new instance. */
    public UpdateScheduleEventRequest() {}

    @Min(0)
    private Long begin;

    @Min(0)
    private Long end;

    @NotNull
    private List<String> missionNamesOrIds;

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
     * Returns the mission names or IDs associated with this schedule event.
     *
     * @return the mission names or IDs
     */
    public List<String> getMissionNamesOrIds() {
        return missionNamesOrIds;
    }

    /**
     * Sets the mission names or IDs associated with this schedule event.
     *
     * @param missionNamesOrIds the mission names or IDs
     */
    public void setMissionNamesOrIds(List<String> missionNamesOrIds) {
        this.missionNamesOrIds = missionNamesOrIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        UpdateScheduleEventRequest that = (UpdateScheduleEventRequest) o;
        return Objects.equals(getBegin(), that.getBegin()) && Objects.equals(getEnd(), that.getEnd()) && Objects.equals(getMissionNamesOrIds(), that.getMissionNamesOrIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBegin(), getEnd(), getMissionNamesOrIds());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("UpdateScheduleEventRequest{");
        sb.append("begin=").append(begin);
        sb.append(", end=").append(end);
        sb.append(", missionNamesOrIds=").append(missionNamesOrIds);
        sb.append('}');
        return sb.toString();
    }

}
