package dev.getelements.elements.service.advancement;

import dev.getelements.elements.dao.MissionDao;
import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.ProgressDao;
import dev.getelements.elements.model.mission.Mission;
import dev.getelements.elements.model.mission.Progress;
import dev.getelements.elements.model.mission.ProgressMissionInfo;
import dev.getelements.elements.model.profile.Profile;
import org.dozer.Mapper;

import javax.inject.Inject;

public class StandardAdvancementService implements AdvancementService {

    private Mapper mapper;

    private ProfileDao profileDao;

    private MissionDao missionDao;

    private ProgressDao progressDao;

    @Override
    public Progress startMission(final Profile profile, final String missionNameOrId) {

        final Progress progress = new Progress();
        final Profile active = getProfileDao().getActiveProfile(profile.getId());
        progress.setProfile(active);

        final Mission mission = getMissionDao().getMissionByNameOrId(missionNameOrId);
        final ProgressMissionInfo progressMissionInfo = getMapper().map(mission, ProgressMissionInfo.class);
        progress.setMission(progressMissionInfo);

        return getProgressDao().createOrGetExistingProgress(progress);

    }

    @Override
    public Progress advanceProgress(final Profile profile, final String missionNameOrId, final int amount) {
        final Progress progress = getProgressDao().getProgressForProfileAndMission(profile, missionNameOrId);
        return getProgressDao().advanceProgress(progress, amount);
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
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
