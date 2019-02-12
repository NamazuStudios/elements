package com.namazustudios.socialengine.service.reward;


import com.namazustudios.socialengine.model.mission.Reward;

public class SuperUserRewardService extends AnonRewardService implements RewardService {
    @Override
    public Reward createReward(Reward reward) {
        return getRewardDao().createReward(reward);
    }

    @Override
    public void delete(final String id) {
        getRewardDao().delete(id);
    }
}
