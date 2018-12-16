package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.PendingReward;
import com.namazustudios.socialengine.rt.annotation.Expose;

/**
 * Manipulates the instances of {@link PendingReward}, including the operations to ensure tha the rewards are properly
 * inserted into the database and credited to a user's inventory.
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
    PendingReward getPendingReward(final String id);

    /**
     * Redeems the {@link PendingReward}.  Once redeemed, the reward will be placed into the associated user's
     * inventory.  This method will arbitrarily select a {@link InventoryItem} to accept the {@link PendingReward}.
     *
     * Additionally this method must guarantee that applying the same {@link PendingReward} multiple times will only
     * credit the user once.
     *
     * @param reward the reward to redeem
     * @return the {@link InventoryItem} to which this {@link PendingReward} was applied.
     */
    InventoryItem redeem(final PendingReward reward);

}
