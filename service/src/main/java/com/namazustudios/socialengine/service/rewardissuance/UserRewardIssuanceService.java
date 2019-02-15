package com.namazustudios.socialengine.service.rewardissuance;

import com.namazustudios.socialengine.dao.RewardIssuanceDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.model.mission.RewardIssuance.State;
import com.namazustudios.socialengine.model.mission.RewardIssuanceResult;

import javax.inject.Inject;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.mission.RewardIssuance.Type.PERSISTENT;

public class UserRewardIssuanceService implements RewardIssuanceService {
    private User user;

    private RewardIssuanceDao rewardIssuanceDao;

    @Override
    public RewardIssuance getRewardIssuance(String id) {
        return getRewardIssuanceDao().getRewardIssuance(id);
    }

    @Override
    public Pagination<RewardIssuance> getRewardIssuances(State state, int offset, int count) {
        final Set<State> states = new HashSet<>();
        if (state != null) {
            states.add(state);
        }

        return getRewardIssuanceDao().getRewardIssuances(getUser(), offset, count, states);
    }

    @Override
    public RewardIssuanceResult redeemRewardIssuance(String id) {
        RewardIssuance rewardIssuance = getRewardIssuanceDao().getRewardIssuance(id);
        return doRedeem(rewardIssuance);
    }

    @Override
    public RewardIssuanceResult redeemRewardIssuance(RewardIssuance rewardIssuance) {
        return doRedeem(rewardIssuance);
    }

    @Override
    public List<RewardIssuanceResult> redeemRewardIssuances(List<String> rewardIssuanceIds) {
        if (rewardIssuanceIds == null || rewardIssuanceIds.size() == 0) {
            throw new IllegalArgumentException("List of Reward Issuance IDs must exist and not be empty.");
        }

        return rewardIssuanceIds
                .stream()
                .map(rii -> getRewardIssuanceDao().getRewardIssuance(rii))
                .map(ri -> doRedeem(ri))
                .collect(Collectors.toList());
    }

    private RewardIssuanceResult doRedeem(RewardIssuance rewardIssuance) {
        final RewardIssuanceResult rewardIssuanceResult = new RewardIssuanceResult();

        rewardIssuanceResult.setRewardIssuanceId(rewardIssuance.getId());

        try {
            InventoryItem resultInventoryItem = getRewardIssuanceDao().redeem(rewardIssuance);
            rewardIssuanceResult.setInventoryItem(resultInventoryItem);

            if (rewardIssuance.getType() == PERSISTENT) {
                RewardIssuance resultRewardIssuance = getRewardIssuanceDao().getRewardIssuance(rewardIssuance.getId());
                rewardIssuanceResult.setRewardIssuance(resultRewardIssuance);
            }
        }
        catch (Exception e) {
            rewardIssuanceResult.setErrorDetails(e.toString());
        }

        return rewardIssuanceResult;
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
