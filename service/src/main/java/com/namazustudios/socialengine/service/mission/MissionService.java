package com.namazustudios.socialengine.service.mission;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Mission;

public interface MissionService {
    /**
     * Returns a list of {@link Mission} objects.
     *
     * @param offset the offset
     * @param count the count
     * @return the list of {@link Mission} instances
     */
    Pagination<Mission> getMissions(int offset, int count);

    /**
     * Returns a list of {@link Mission} objects.
     *
     * @param offset the offset
     * @param count the count
     * @param query the search query
     * @return the list of {@link Mission} instances
     */
    Pagination<Mission> getMissions(int offset, int count, String query);

    /**
     * Gets a mission with specified name or ID.
     *
     * @param missionNameOrId the UserId
     *
     * @return the mission
     */
    Mission getMissionByNameOrId(String missionNameOrId);

    /**
     * Updates the {@link Mission}. The {@link Mission#getId()} method is
     * used to key the {@link Mission}.
     *
     * @param mission the {@link Mission} to update
     * @return the {@link Mission} as it was written to the database
     */
    Mission updateMission(Mission mission);

    /**
     * Creates a new {@link Mission}.  The ID of the mission, as specified by {@link Mission#getId()},
     * should be null and will be assigned.
     *
     * @param mission the {@link Mission} to create
     * @return the {@link Mission} as it was created by the service.
     */
    Mission createMission(Mission mission);

    /**
     * Deletes the {@link Mission} with the supplied id or name.
     *
     * @param missionNameOrId the mission name or ID.
     */
    void deleteMission(String missionNameOrId);

}
