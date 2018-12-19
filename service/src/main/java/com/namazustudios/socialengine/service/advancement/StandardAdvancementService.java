package com.namazustudios.socialengine.service.advancement;

import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.ProgressDao;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.mission.ProgressMissionInfo;
import com.namazustudios.socialengine.model.profile.Profile;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;

import static java.util.stream.Collectors.toList;

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
        final Progress progress = getProgressDao().getProgresseForProfileAndMission(profile, missionNameOrId);
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
