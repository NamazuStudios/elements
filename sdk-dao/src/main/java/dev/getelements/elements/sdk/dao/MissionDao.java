package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.mission.UpdateMissionRequest;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Created by davidjbrooks on 11/24/18.
 */

@ElementServiceExport
public interface MissionDao {

    /**
     * Gets missions specifying the offset and the count.
     *
     * @param offset the offset
     * @param count  the count
     * @param tags
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<Mission> getMissions(int offset, int count, List<String> tags);

    /**
     * Gets missions specifying the offset and the count, specifying a search filter.
     *
     * @param offset the offset
     * @param count  the count
     * @param search a query to filter the results
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<Mission> getMissions(int offset, int count, String search);

    /**
     * Gets all {@link Mission}s matching the list of names or ids.
     *
     * @param missionNamesOrIds a list containing missions or ids
     * @return the {@link List<Mission>}
     */
    List<Mission> getMissionsMatching(Collection<String> missionNamesOrIds);

    Optional<Mission> findMissionByNameOrId(String missionNameOrId);

    /**
     * Gets the mission with the id, or throws a {@link NotFoundException} if the
     * mission can't be found.
     *
     * @return the {@link Mission} that was requested, never null
     */
    default Mission getMissionByNameOrId(final String missionNameOrId) {
        return findMissionByNameOrId(missionNameOrId).orElseThrow(NotFoundException::new);
    }

    /**
     * Updates the mission, or throws a {@link NotFoundException} if the
     * mission can't be found.  The missionNameOrId is used to key the mission being updated.
     *
     * @return the {@link Mission} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Mission is invalid
     */
    Mission updateMission(Mission mission);

    /**
     * Creates a mission.  The value of {@link Mission#getId()} will be ignored.
     *
     * @return the {@link Mission} as it was written into the database
     * @throws InvalidDataException if the state of the passed in Mission is invalid
     * @throws DuplicateException   if the passed in Mission has a name that already exists
     */
    Mission createMission(Mission mission);

    /**
     * Deletes a mission.
     *
     * @param missionId the mission ID
     */
    void deleteMission(String missionId);

}
