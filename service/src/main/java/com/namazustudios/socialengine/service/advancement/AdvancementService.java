package com.namazustudios.socialengine.service.advancement;

import com.namazustudios.socialengine.model.advancement.Advancement;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.Step;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.annotation.Expose;

import java.util.List;

/**
 * Provides logic to advance a {@link Profile} through a {@link Mission}.
 */
@Expose(modules = {
    "namazu.elements.service.advancement",
    "namazu.socialengine.service.advancement",
})
public interface AdvancementService {

    /**
     * Begins a {@link Mission} by assigning and creating a new instance of {@link Progress}.  This is shorthand
     * for looking up a mission by name ({@link Mission#getName()}), setting the {@link Profile} and saving to the
     * database.
     *
     * @param profile the {@link Profile} of the person starting the mission
     * @param missionNameOrId the unique name of the {@link Mission}, as determined by {@link Mission#getName()}
     */
    Progress startMission(Profile profile, String missionNameOrId);

    /**
     * Advances {@link Progress} by decrementing the value specified by the amount, and returning the prizes rewarded
     * in the form of an {@link Advancement} object.  Specifically, this will involve the follwing operations:
     *
     * <ul>
     *     <li>If the Profile has no {@link Progress} for the supplied mission, then there shall be no side-effects.</li>
     *     <li>Otherwise, decrement the value tracked by {@link Progress#getRemaining()} using the specified amount</li>
     *     <li>If, after subtraction, the result is <= 0 advance to the next {@link Step} in the {@link Mission}</li>
     *     <li>Carry over any remaining actions to the next step.</li>
     *     <li>Continue rewarding {@link Reward} instances until all actions are consumed</li>
     *     <li>Continue tracking complete {@link Step}s until all actions are consumed</li>
     * </ul>
     *
     * Additionally, the operation must be performed atomically without fear of race conditions.
     *
     * @param profile the {@link Profile} tracking the {@link Mission}
     * @param missionNameOrId the {@link Mission}'s name as determined by {@link Mission#getName()}.
     * @param amount the amount of actions to apply to the {@link Progress#getRemaining()}
     * @return the {@link Advancement}, never null
     */
    List<Progress> advanceProgress(Profile profile, String missionNameOrId, int amount);

}
