package dev.getelements.elements.dao;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.Schedule;
import dev.getelements.elements.model.profile.Profile;

import java.util.List;

/**
 * Manages {@link Progress} instances for the
 */
public interface ScheduleProgressDao {

    /**
     * Gets all {@link Progress} instances with the supplied profile, schedule, and offet, count
     * @param profileId
     * @param scheduleNameOrId
     * @param offset
     * @param count
     * @return
     */
    Pagination<Progress> getProgresses(String profileId, String scheduleNameOrId, int offset, int count);

    /**
     * Creates {@link Progress} instances for {@link Mission}s in the supplied list. If the missions are already
     * assigned, then no changes will be made. If a {@link Progress} with the mission is already assigned, then
     * this will ensure that the {@link Mission} is now linked to the {@link Schedule} if not already assigned.
     *
     * @param scheduleNameOrId the {@link Schedule} name or ID
     * @param profileId {@link Profile} identifier
     * @param missions the {@link Mission}s to assign
     */
    void createProgressesForMissionsIn(String scheduleNameOrId, String profileId, List<Mission> missions);

    /**
     * Deletes {@link Progress} instances for {@link Mission}s not in the supplied list. If no other {@link Schedule}
     * links the {@link Mission}, then this will permamently remove the {@link Progress} instances.
     *
     * @param scheduleNameOrId the {@link Schedule} name or ID
     * @param profileId {@link Profile} identifier
     * @param missions the {@link Mission}s to keep
     */
    void deleteProgressesForMissionsNotIn(String scheduleNameOrId, String profileId, List<Mission> missions);

}
