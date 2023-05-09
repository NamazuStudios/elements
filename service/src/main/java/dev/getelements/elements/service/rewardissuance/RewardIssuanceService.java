package dev.getelements.elements.service.rewardissuance;

import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.reward.RewardIssuance;
import dev.getelements.elements.model.reward.RewardIssuance.State;
import dev.getelements.elements.model.reward.RewardIssuanceRedemptionResult;

import java.util.List;
import java.util.Set;

public interface RewardIssuanceService {
    /**
     * Fetches the instance of {@link RewardIssuance} for the given {@param id}.
     *
     * @param id the id of the {@link RewardIssuance} as specified by {@link RewardIssuance#getId()}.
     *
     * @return the {@link RewardIssuance}.
     */
    RewardIssuance getRewardIssuance(String id);

    /**
     * Fetches all {@link RewardIssuance}s for the logged-in user, filtered by the optional given {@param states} and
     * {@param tags}.
     *
     *
     * @return {@link Pagination} of {@link RewardIssuance}s.
     */
    Pagination<RewardIssuance> getRewardIssuances(int offset, int count, List<State> states, List<String> tags);

    /**
     * Redeems the given {@link RewardIssuance} by {@param id}.
     *
     * @param id
     * @return the redemption {@link RewardIssuanceRedemptionResult}.
     */
    RewardIssuanceRedemptionResult redeemRewardIssuance(String id);

    /**
     * Redeems the given list of {@param rewardIssuanceIds} for {@link RewardIssuance}s.
     *
     * @param rewardIssuanceIds
     * @return the list of redemption {@link RewardIssuanceRedemptionResult}s.
     */
    List<RewardIssuanceRedemptionResult> redeemRewardIssuances(List<String> rewardIssuanceIds);
}
