package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Progress;

public interface ScheduleProgressService {

    Pagination<Progress> getScheduleProgressService(String scheduleNameOrId, int offset, int count);

}
