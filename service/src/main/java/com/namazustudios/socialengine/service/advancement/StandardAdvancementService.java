package com.namazustudios.socialengine.service.advancement;

import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.model.advancement.Advancement;
import com.namazustudios.socialengine.model.mission.Progress;
import com.namazustudios.socialengine.model.profile.Profile;

import javax.inject.Inject;

public class StandardAdvancementService implements AdvancementService {

    private ProfileDao profileDao;

    private MissionDao missionDao;

    @Override
    public Progress startMission(final Profile profile, final String mission) {
        
        return null;
    }

    @Override
    public Advancement advanceProgress(final Profile profile, final String mission, final int amount) {
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

}
