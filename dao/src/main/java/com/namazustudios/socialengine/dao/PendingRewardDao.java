package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.PendingReward;
import com.namazustudios.socialengine.model.mission.PendingReward.State;
import com.namazustudios.socialengine.rt.annotation.Expose;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Manipulates the instances of {@link PendingReward}, including the operations to ensure tha the rewards are properly
 * inserted into the database and credited to a user's inventory in an atomic way.
 */
@Expose(modules = {
        "namazu.elements.dao.pendingreward",
        "namazu.socialengine.dao.pendingreward",
})
public interface PendingRewardDao {

    /**
     * Fetches the instance of {@link PendingReward}.
     *
     * @param id the id of hte {@link PendingReward} as specified by {@link PendingReward#getId()}.
     *
     * @return the pending reward
     */
    PendingReward getPendingReward(String id);

    /**
     * Gets all pending rewards.
     *
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link PendingReward}
     */
    default Pagination<PendingReward> getPendingRewards(final User user, final int offset, final int count) {
        return getPendingRewards(user, offset, count, emptySet());
    }

    /**
     * Gets all pending rewards, specifying the {@link State}
     *
     * @param offset the offset
     * @param count the count
     * @param states if non-empty, will include the requested states.  Otehrwise all states will be included.
     * @return a {@link Pagination} of {@link PendingReward}
     */
    Pagination<PendingReward> getPendingRewards(User user, int offset, int count, Set<State> states);

    /**
     * Creates an instance of {@link PendingReward}. This must be created with the flag {@link State#CREATED}.
     *
     * @param pendingReward the instance of {@link PendingReward} to create
     * @return the {@link PendingReward} instance
     */
    PendingReward createPendingReward(PendingReward pendingReward);

    /**
     * Flags an instance of {@link PendingReward} as {@link State#PENDING}
     *
     * @param pendingReward the instance of {@link PendingReward} to create
     * @return the {@link PendingReward} instance
     */
    PendingReward flagPending(PendingReward pendingReward);

    /**
     * Redeems the {@link PendingReward}.  Once redeemed, the reward will be placed into the associated user's
     * inventory.  This method will select an {@link InventoryItem} with a priority of value zero to accept the
     * {@link PendingReward}.
     *
     * Additionally this method must guarantee that applying the same {@link PendingReward} multiple times will only
     * credit the user once.
     *
     * @param reward the reward to redeem
     * @return the {@link InventoryItem} to which this {@link PendingReward} was applied.
     */
    InventoryItem redeem(final PendingReward reward);

}
