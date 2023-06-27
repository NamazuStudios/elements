package dev.getelements.elements.service.leaderboard;

import dev.getelements.elements.dao.ScoreDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.leaderboard.Score;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.ScoreService;

import javax.inject.Inject;
import javax.inject.Provider;
import java.util.Objects;
import java.util.function.Supplier;

public class UserScoreService implements ScoreService {

    private ScoreDao scoreDao;

    private Supplier<Profile> currentProfileSupplier;

    @Override
    public Score createOrUpdateScore(final String leaderboardNameOrId, final Score score) {

        final Profile profile = getCurrentProfileSupplier().get();
        final Profile scoreProfile = score.getProfile();

        if (scoreProfile == null) {
            score.setProfile(profile);
        } else if (!Objects.equals(profile.getId(), scoreProfile.getId())) {
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

    public Supplier<Profile> getCurrentProfileSupplier() {
        return currentProfileSupplier;
    }

    @Inject
    public void setCurrentProfileSupplier(Supplier<Profile> currentProfileSupplier) {
        this.currentProfileSupplier = currentProfileSupplier;
    }

}
