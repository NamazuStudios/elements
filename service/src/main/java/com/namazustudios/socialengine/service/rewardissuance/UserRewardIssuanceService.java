package com.namazustudios.socialengine.service.rewardissuance;

import com.namazustudios.socialengine.dao.RewardIssuanceDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.reward.RewardIssuance;
import com.namazustudios.socialengine.model.reward.RewardIssuance.State;
import com.namazustudios.socialengine.model.reward.RewardIssuanceRedemptionResult;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.reward.RewardIssuance.Type.PERSISTENT;

public class UserRewardIssuanceService implements RewardIssuanceService {
    private User user;

    private RewardIssuanceDao rewardIssuanceDao;

    @Override
    public RewardIssuance getRewardIssuance(String id) {
        return getRewardIssuanceDao().getRewardIssuance(id);
    }

    @Override
    public Pagination<RewardIssuance> getRewardIssuances(
            final int offset,
            final int count,
            final List<State> states,
            final List<String> tags) {
        return getRewardIssuanceDao().getRewardIssuances(getUser(), offset, count, states, tags);
    }

    @Override
    public RewardIssuanceRedemptionResult redeemRewardIssuance(String id) {
        return doRedeem(id);
    }

    @Override
    public List<RewardIssuanceRedemptionResult> redeemRewardIssuances(List<String> rewardIssuanceIds) {
        if (rewardIssuanceIds == null || rewardIssuanceIds.size() == 0) {
            throw new IllegalArgumentException("List of Reward Issuance IDs must exist and not be empty.");
        }

        return rewardIssuanceIds
                .stream()
                .map(rii -> doRedeem(rii))
                .collect(Collectors.toList());
    }

    private RewardIssuanceRedemptionResult doRedeem(String rewardIssuanceId) {
        final RewardIssuanceRedemptionResult rewardIssuanceRedemptionResult = new RewardIssuanceRedemptionResult();

        rewardIssuanceRedemptionResult.setRewardIssuanceId(rewardIssuanceId);

        try {
            RewardIssuance rewardIssuance = getRewardIssuanceDao().getRewardIssuance(rewardIssuanceId);
            InventoryItem resultInventoryItem = getRewardIssuanceDao().redeem(rewardIssuance);
            rewardIssuanceRedemptionResult.setInventoryItem(resultInventoryItem);

            if (rewardIssuance.getType() == PERSISTENT) {
                RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
                rewardIssuanceRedemptionResult.setRewardIssuance(resultRewardIssuance);
            }
        }
        catch (Exception e) {
            rewardIssuanceRedemptionResult.setErrorDetails(e.toString());
        }

        return rewardIssuanceRedemptionResult;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public RewardIssuanceDao getRewardIssuanceDao() {
        return rewardIssuanceDao;
    }

    @Inject
    public void setRewardIssuanceDao(RewardIssuanceDao rewardIssuanceDao) {
        this.rewardIssuanceDao = rewardIssuanceDao;
    }
}
