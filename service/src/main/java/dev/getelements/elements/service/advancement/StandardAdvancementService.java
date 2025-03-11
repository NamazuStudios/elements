package dev.getelements.elements.service.advancement;

import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.ProgressDao;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.Progress;
import dev.getelements.elements.sdk.model.mission.ProgressMissionInfo;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import dev.getelements.elements.sdk.service.advancement.AdvancementService;
import jakarta.inject.Inject;

public class StandardAdvancementService implements AdvancementService {

    private MapperRegistry mapperRegistry;

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

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
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
