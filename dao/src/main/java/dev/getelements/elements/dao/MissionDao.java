package dev.getelements.elements.dao;

import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

import java.util.List;

/**
 * Created by davidjbrooks on 11/24/18.
 */
@Expose({
    @ModuleDefinition("namazu.elements.dao.mission"),
    @ModuleDefinition(
        value = "namazu.socialengine.dao.mission",
        deprecated = @DeprecationDefinition("Use namazu.elements.dao.mission instead"))
})
public interface MissionDao {

    /**
     * Gets missions specifying the offset and the count.
     *
     * @param offset the offset
     * @param count the count
     * @param tags
     * @return a {@link Pagination} of {@link Mission} objects.
     */
    Pagination<Mission> getMissions(int offset, int count, List<String> tags);

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
