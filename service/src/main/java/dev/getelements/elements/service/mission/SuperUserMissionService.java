package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.service.mission.MissionService;

public class SuperUserMissionService extends AnonMissionService implements MissionService {

    @Override
    public Mission updateMission(final String missionNameOrId, final Mission mission) { return missionDao.updateMission(missionNameOrId, mission); }

    @Override
    public Mission createMission(final Mission mission) { return missionDao.createMission(mission); }

    @Override
    public void deleteMission(final String missionNameOrId) { missionDao.deleteMission(missionNameOrId); }

}
