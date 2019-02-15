package com.namazustudios.socialengine.service.reward;

import com.namazustudios.socialengine.dao.RewardDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.mission.Reward;

import javax.inject.Inject;

public class AnonRewardService implements RewardService {

    protected User user;

    protected RewardDao rewardDao;

    @Override
    public Reward getReward(String id) {
        return getRewardDao().getReward(id);
    }

    @Override
    public Reward createReward(Reward reward) {
        throw new ForbiddenException("Unprivileged requests are unable to create rewards.");
    }

    @Override
    public void delete(final String id) {
        throw new ForbiddenException("Unprivileged requests are unable to delete rewards.");
    }

    public RewardDao getRewardDao() {
        return rewardDao;
    }

    @Inject
    public void setRewardDao(RewardDao rewardDao) {
        this.rewardDao = rewardDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
