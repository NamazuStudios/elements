package dev.getelements.elements.sdk.dao;

import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.InventoryItem;
import dev.getelements.elements.sdk.model.reward.RewardIssuance;
import dev.getelements.elements.sdk.model.reward.RewardIssuance.State;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import java.util.List;

import static java.util.Collections.emptyList;

/**
 * Manipulates the instances of {@link RewardIssuance}, including the operations to ensure tha the rewards are properly
 * inserted into the database and credited to a user's inventory in an atomic way.
 */

@ElementServiceExport
public interface RewardIssuanceDao {

    /**
     * Fetches the instance of {@link RewardIssuance}.
     *
     * @param id the id of the {@link RewardIssuance} as specified by {@link RewardIssuance#getId()}.
     * @return the reward issuance
     */
    RewardIssuance getRewardIssuance(String id);

    /**
     * Fetches the reward issuance for the given user and context.
     *
     * @param user
     * @param context
     * @return the reward issuance
     */
    RewardIssuance getRewardIssuance(User user, String context);

    /**
     * Gets all reward issuances for a given user.
     *
     * @param user   the user
     * @param offset the offset
     * @param count  the count
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    default Pagination<RewardIssuance> getRewardIssuances(final User user, final int offset, final int count) {
        return getRewardIssuances(user, offset, count, emptyList(), emptyList());
    }

    /**
     * Gets all reward issuances for the given user, optionally specifying the allowable {@link State}s and/or
     * {@param tags}.
     *
     * @param offset the offset
     * @param count  the count
     * @param states if non-empty, will include the requested states in the query. Otherwise states will be ignored in
     *               the query.
     * @param tags   if non-empty, will include the requested set of tags in the query. Otherwise tags will be ignored in
     *               the query.
     * @return a {@link Pagination} of {@link RewardIssuance}
     */
    Pagination<RewardIssuance> getRewardIssuances(
            User user,
            int offset,
            int count,
            List<State> states,
            List<String> tags);

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
     *
     * @param rewardIssuance      the issuance to update
     * @param expirationTimestamp the expiration, in ms
     * @return
     */
    RewardIssuance updateExpirationTimestamp(RewardIssuance rewardIssuance, long expirationTimestamp);


    /**
     * Redeems the {@link RewardIssuance}.  Once redeemed, the reward will be placed into the associated user's
     * inventory.  This method will select an {@link InventoryItem} with a priority of value zero to accept the
     * {@link RewardIssuance}.
     * <p>
     * Additionally this method must guarantee that applying the same {@link RewardIssuance} multiple times will only
     * credit the user once.
     *
     * @param rewardIssuance the reward to redeem
     * @return the {@link InventoryItem} to which this {@link RewardIssuance} was applied.
     */
    InventoryItem redeem(final RewardIssuance rewardIssuance);

    /**
     * Deletes a {@link RewardIssuance} with the supplied id.
     *
     * @param id the id
     */
    void delete(final String id);

}
