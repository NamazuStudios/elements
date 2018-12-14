package com.namazustudios.socialengine.service.advancement;

import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.model.advancement.Advancement;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.mission.Step;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.rt.exception.InternalException;

import javax.inject.Inject;
import java.util.List;

public class StandardAdvancementService implements AdvancementService {

    private ProfileDao profileDao;

    private MissionDao missionDao;

    private ProgressDao progressDao;

    @Override
    public Progress startMission(final Profile profile, final String missionNameOrId) {

        final Progress progress = new Progress();

        final Profile active = getProfileDao().getActiveProfile(profile.getId());
        progress.setProfile(active);

        final Mission mission = getMissionDao().getMissionByNameOrId(missionNameOrId);
        progress.setMission(mission);

        if (mission.getSteps() == null) {

            final Step step = mission.getFinalRepeatStep();

            if (step == null) {
                // This should not be necessary.  See SOC-249
                throw new InternalException("Corrupted Mission.  Missing steps. (" + mission.getName() + ")");
            }

            progress.setCurrentStep(step);
            progress.setRemaining(step.getCount());

        } else {

            final List<Step> missionSteps = mission.getSteps();

            if (missionSteps == null || missionSteps.isEmpty()) {
                // This should not be necessary.  See SOC-249
                throw new InternalException("Corrupted Mission.  Missing steps. (" + mission.getName() + ")");
            }

            final Step step = missionSteps.get(0);

            progress.setCurrentStep(step);
            progress.setRemaining(step.getCount());

        }

        return getProgressDao().createProgress(progress);

    }

    @Override
    public Advancement advanceProgress(final Profile profile, final String mission, final int amount) {


        for (int remaining = amount; remaining > 0;) {

        }

        return null;

    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }

    public MissionDao getMissionDao() {
        return missionDao;
    }

    @Inject
    public void setMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

    public ProgressDao getProgressDao() {
        return progressDao;
    }

    @Inject
    public void setProgressDao(ProgressDao progressDao) {
        this.progressDao = progressDao;
    }

}
