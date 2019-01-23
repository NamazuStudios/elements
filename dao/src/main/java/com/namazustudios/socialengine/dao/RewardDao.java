package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.rt.annotation.Expose;

import java.util.Set;

import static java.util.Collections.emptySet;

/**
 * Manipulates the instances of {@link Reward}.
 */
@Expose(modules = {
        "namazu.elements.dao.reward",
        "namazu.socialengine.dao.reward",
})
public interface RewardDao {

    /**
     * Fetches the instance of {@link Reward}.
     *
     * @param id the id of the {@link Reward} as specified by {@link Reward#getId()}.
     *
     * @return the pending reward
     */
    Reward getReward(String id);


    /**
     * Deletes a {@link Reward} with the supplied id.
     *
     * @param id the id
     */
    void delete(final String id);

}
