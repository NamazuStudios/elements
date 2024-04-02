package dev.getelements.elements.dao.mongo.mission;

import dev.getelements.elements.dao.ScheduleEventDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.ScheduleEvent;

public class MongoScheduleEventDao implements ScheduleEventDao {

    @Override
    public ScheduleEvent createScheduleEvent(final ScheduleEvent scheduleEvent) {
        return null;
    }

    @Override
    public ScheduleEvent updateScheduleEvent(final ScheduleEvent scheduleEvent) {
        return null;
    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(final String scheduleNameOrId,
                                                       final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<ScheduleEvent> getScheduleEvents(final String scheduleNameOrId,
                                                       final int offset, final int count,
                                                       final String search) {
        return null;
    }

    @Override
    public ScheduleEvent getScheduleEventByNameOrId(final String scheduleNameOrId,
                                                    final String scheduleEventNameOrId) {
        return null;
    }

    @Override
    public void deleteScheduleEvent(String scheduleNameOrId) {

    }

}
