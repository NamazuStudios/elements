package dev.getelements.elements.model.mission;

import javax.validation.constraints.Min;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.Objects;

public class CreateScheduleEventRequest {

    @Min(0)
    private Long begin;

    @Min(0)
    private Long end;

    @NotNull
    private List<@NotNull String> missionNamesOrIds;

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

    public List<String> getMissionNamesOrIds() {
        return missionNamesOrIds;
    }

    public void setMissionNamesOrIds(List<String> missionNamesOrIds) {
        this.missionNamesOrIds = missionNamesOrIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CreateScheduleEventRequest that = (CreateScheduleEventRequest) o;
        return Objects.equals(getBegin(), that.getBegin()) && Objects.equals(getEnd(), that.getEnd()) && Objects.equals(getMissionNamesOrIds(), that.getMissionNamesOrIds());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getBegin(), getEnd(), getMissionNamesOrIds());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("CreateScheduleEventRequest{");
        sb.append("begin=").append(begin);
        sb.append(", end=").append(end);
        sb.append(", missionIds=").append(missionNamesOrIds);
        sb.append('}');
        return sb.toString();
    }

}
