package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.rt.annotation.Expose;

/**
 * Created by davidjbrooks on 11/24/18.
 */
@Expose(modules = {
        "namazu.elements.dao.mission",
        "namazu.socialengine.dao.mission",
})
public interface MissionDao {

    /**
     * Gets missions specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<Mission> getMissions(int offset, int count);

    /**
     * Gets missions specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count the count
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<Mission> getMissions(int offset, int count, String search);

    /**
     * Gets the mission with the id, or throws a {@link NotFoundException} if the
     * mission can't be found.
     *
     * @return the {@link Mission} that was requested, never null
     */
    Mission getMissionByNameOrId(String missionId);

    /**
     * Updates the mission, or throws a {@link NotFoundException} if the
     * mission can't be found.  The {@link Mission#getId()} is used to key the mission being updated.
     *
     * @return the {@link Mission} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in Mission is invalid
     */
    Mission updateMission(Mission mission);

    /**
     * Creates a mission.  The value of {@link Mission#getId()} will be ignored.
     *
     * @return the {@link Mission} as it was written into the database
     * @throws InvalidDataException
     *     if the state of the passed in Mission is invalid
     * @throws DuplicateException
     *     if the passed in Mission has a name that already exists
     */
    Mission createMission(Mission mission);

    /**
     * Deletes a mission.
     *
     * @param missionId the mission ID
     */
    void deleteMission(String missionId);

}
