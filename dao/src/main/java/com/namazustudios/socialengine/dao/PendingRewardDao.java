package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.RewardIssuance.State;
import com.namazustudios.socialengine.rt.annotation.Expose;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Manipulates the instances of {@link RewardIssuance}, including the operations to ensure tha the rewards are properly
 * inserted into the database and credited to a user's inventory in an atomic way.
 */
@Expose(modules = {
        "namazu.elements.dao.pendingreward",
        "namazu.socialengine.dao.pendingreward",
})
public interface PendingRewardDao {

    /**
     * Fetches the instance of {@link RewardIssuance}.
     *
     * @param id the id of hte {@link RewardIssuance} as specified by {@link RewardIssuance#getId()}.
     *
     * @return the pending reward
     */
    RewardIssuance getPendingReward(String id);

    /**
     * Gets all pending rewards.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    default Pagination<RewardIssuance> getPendingRewards(final User user, final int offset, final int count) {
        return getPendingRewards(user, offset, count, emptySet());
    }

    /**
     * Gets all pending rewards, specifying the {@link State}
     *
     * @param offset the offset
     * @param count the count
     * @param states if non-empty, will include the requested states.  Otehrwise all states will be included.
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    Pagination<RewardIssuance> getPendingRewards(User user, int offset, int count, Set<State> states);

    /**
     * Creates an instance of {@link RewardIssuance}. This must be created with the flag {@link State#CREATED}.
     *
     * @param rewardIssuance the instance of {@link RewardIssuance} to create
     * @return the {@link RewardIssuance} instance
     */
    RewardIssuance createPendingReward(RewardIssuance rewardIssuance);

    /**
     * Flags an instance of {@link RewardIssuance} as {@link State#PENDING}
     *
     * @param rewardIssuance the instance of {@link RewardIssuance} to create
     * @return the {@link RewardIssuance} instance
     */
    RewardIssuance flagPending(RewardIssuance rewardIssuance);

    /**
     * Redeems the {@link RewardIssuance}.  Once redeemed, the reward will be placed into the associated user's
     * inventory.  This method will select an {@link InventoryItem} with a priority of value zero to accept the
     * {@link RewardIssuance}.
     *
     * Additionally this method must guarantee that applying the same {@link RewardIssuance} multiple times will only
     * credit the user once.
     *
     * @param reward the reward to redeem
     * @return the {@link InventoryItem} to which this {@link RewardIssuance} was applied.
     */
    InventoryItem redeem(final RewardIssuance reward);

    /**
     * Deltes a {@link RewardIssuance} wiht the supplied id.
     *
     * @param id the id
     */
    void delete(final String id);

}
