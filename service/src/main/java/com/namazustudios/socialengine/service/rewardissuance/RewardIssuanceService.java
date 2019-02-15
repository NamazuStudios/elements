package com.namazustudios.socialengine.service.rewardissuance;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.RewardIssuance.State;
import com.namazustudios.socialengine.model.mission.RewardIssuanceResult;

import java.util.List;

public interface RewardIssuanceService {
    /**
     * Fetches the instance of {@link RewardIssuance} for the given {@param id}.
     *
     * @param id the id of the {@link RewardIssuance} as specified by {@link RewardIssuance#getId()}.
     *
     * @return the reward issuance
     */
    RewardIssuance getRewardIssuance(String id);

    /**
     * Fetches all {@link RewardIssuance}s for the logged-in user, filtered by the optional given {@param state}.
     *
     *
     * @return pagination of reward issuances
     */
    Pagination<RewardIssuance> getRewardIssuances(State state, int offset, int count);

    /**
     * Redeems the given {@link RewardIssuance} by {@param id}.
     *
     * @param id
     * @return the updated reward issuance
     */
    RewardIssuanceResult redeemRewardIssuance(String id);

    /**
     * Redeems the given {@link RewardIssuance}.
     *
     * @param rewardIssuance
     * @return the updated reward issuance
     */
    RewardIssuanceResult redeemRewardIssuance(RewardIssuance rewardIssuance);

    /**
     * Redeems the given list of {@param rewardIssuanceIds} for {@link RewardIssuance}s.
     *
     * @param rewardIssuanceIds
     * @return the updated reward issuances
     */
    List<RewardIssuanceResult> redeemRewardIssuances(List<String> rewardIssuanceIds);
}
