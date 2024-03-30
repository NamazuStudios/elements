package dev.getelements.elements.model.mission;

import java.util.Objects;

public class ScheduleEvent {

    private String id;

    private Long begin;

    private Long end;

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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ScheduleEvent scheduleEvent = (ScheduleEvent) o;
        return Objects.equals(getId(), scheduleEvent.getId()) && Objects.equals(getBegin(), scheduleEvent.getBegin()) && Objects.equals(getEnd(), scheduleEvent.getEnd());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getBegin(), getEnd());
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("Event{");
        sb.append("id='").append(id).append('\'');
        sb.append(", begin=").append(begin);
        sb.append(", end=").append(end);
        sb.append('}');
        return sb.toString();
    }

}
