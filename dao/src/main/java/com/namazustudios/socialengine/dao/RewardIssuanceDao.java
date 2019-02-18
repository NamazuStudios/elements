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
        "namazu.elements.dao.rewardissuance",
        "namazu.socialengine.dao.rewardissuance",
})
public interface RewardIssuanceDao {

    /**
     * Fetches the instance of {@link RewardIssuance}.
     *
     * @param id the id of the {@link RewardIssuance} as specified by {@link RewardIssuance#getId()}.
     *
     * @return the reward issuance
     */
    RewardIssuance getRewardIssuance(String id);

    /**
     * Fetches the reward issuance for the given user and context.
     * @param user
     * @param context
     * @return the reward issuance
     */
    RewardIssuance getRewardIssuance(User user, String context);

    /**
     * Gets reward issuances for the given user specifying the offset, count, and set of tags.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
     * @param tags set of tags
     * @return a {@link Pagination} of {@link RewardIssuance} objects.
     */
    Pagination<RewardIssuance> getRewardIssuancesByTags(final User user, int offset, int count, Set<String> tags);

    /**
     * Gets all reward issuances for a given user.
     *
     * @param user the user
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    default Pagination<RewardIssuance> getRewardIssuances(final User user, final int offset, final int count) {
        return getRewardIssuances(user, offset, count, emptySet());
    }

    /**
     * Gets all reward issuances, specifying the {@link State}
     *
     * @param offset the offset
     * @param count the count
     * @param states if non-empty, will include the requested states.  Otherwise all states will be included.
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    Pagination<RewardIssuance> getRewardIssuances(User user, int offset, int count, Set<State> states);

    /**
     * Gets or creates an instance of {@link RewardIssuance}. If created, the issuance will be set to a
     * state of {@link State#ISSUED}.
     *
     * @param rewardIssuance the instance of {@link RewardIssuance} to create
     * @return the {@link RewardIssuance} instance
     */
    RewardIssuance getOrCreateRewardIssuance(RewardIssuance rewardIssuance);

    /**
     * Updates the given issuance to a new expiration timestamp. If a negative value is provided, the value will be
     * unset in the db.
     * @param rewardIssuance the issuance to update
     * @param expirationTimestamp the expiration, in ms
     * @return
     */
    RewardIssuance updateExpirationTimestamp(RewardIssuance rewardIssuance, long expirationTimestamp);


    /**
     * Redeems the {@link RewardIssuance}.  Once redeemed, the reward will be placed into the associated user's
     * inventory.  This method will select an {@link InventoryItem} with a priority of value zero to accept the
     * {@link RewardIssuance}.
     *
     * Additionally this method must guarantee that applying the same {@link RewardIssuance} multiple times will only
     * credit the user once.
     *
     * @param rewardIssuance the reward to redeem
     * @return the {@link InventoryItem} to which this {@link RewardIssuance} was applied.
     */
    InventoryItem redeem(final RewardIssuance rewardIssuance);

    /**
     * Deltes a {@link RewardIssuance} wiht the supplied id.
     *
     * @param id the id
     */
    void delete(final String id);

}
