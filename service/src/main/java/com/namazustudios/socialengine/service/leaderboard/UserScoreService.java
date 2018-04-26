package com.namazustudios.socialengine.service.leaderboard;

import com.namazustudios.socialengine.dao.ScoreDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.leaderboard.Score;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.service.ScoreService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;

public class UserScoreService implements ScoreService {

    private ScoreDao scoreDao;

    private Provider<Profile> profileProvider;

    @Override
    public Score createOrUpdateScore(final String leaderboardNameOrId, final Score score) {

        final Profile profile = getProfileProvider().get();
        final Profile scoreProfile = score.getProfile();

        if (scoreProfile == null) {
            score.setProfile(profile);
        } else if (!Objects.equals(profile.getId(), score.getId())) {
            throw new ForbiddenException("Profiles does not match.");
        } else {
            getScoreDao().createOrUpdateScore(leaderboardNameOrId, score);
        }

        return getScoreDao().createOrUpdateScore(leaderboardNameOrId, score);

    }

    public ScoreDao getScoreDao() {
        return scoreDao;
    }

    @Inject
    public void setScoreDao(ScoreDao scoreDao) {
        this.scoreDao = scoreDao;
    }

    public Provider<Profile> getProfileProvider() {
        return profileProvider;
    }

    @Inject
    public void setProfileProvider(Provider<Profile> profileProvider) {
        this.profileProvider = profileProvider;
    }

}
