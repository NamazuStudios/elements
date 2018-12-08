package com.namazustudios.socialengine.service.advancement;

import com.namazustudios.socialengine.model.advancement.Advancement;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.Step;
import com.namazustudios.socialengine.model.profile.Profile;

/**
 * Provides logic to advance a {@link Profile} through a {@link Mission}.
 */
public interface AdvancementService {

    /**
     * Begins a {@link Mission} by assigning and creating a new instance of {@link Progress}.  This is shorthand
     * for looking up a mission by name ({@link Mission#getName()}), setting the {@link Profile} and saving to the
     * database.
     *
     * @param profile the {@link Profile} of the person starting the mission
     * @param mission the unique name of the {@link Mission}, as determined by {@link Mission#getName()}
     */
    Progress startMission(Profile profile, String mission);

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
     * @param mission the {@link Mission}'s name as determined by {@link Mission#getName()}.
     * @param amount the amount of actions to apply to the {@link Progress#getRemaining()}
     * @return the {@link Advancement}, never null
     */
    Advancement advanceProgress(Profile profile, String mission, int amount);

}
