package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;

public class UserScheduleProgressService implements ScheduleProgressService {
    @Override
    public Pagination<Progress> getScheduleProgressService(
            final String scheduleNameOrId,
            final int offset, final int count) {
        // TODO Implement This
        throw new UnsupportedOperationException("Not implemented.");
    }

}
