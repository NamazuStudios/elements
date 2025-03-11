package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.service.mission.MissionService;

public class SuperUserMissionService extends AnonMissionService implements MissionService {

    @Override
    public Mission updateMission(Mission mission) { return missionDao.updateMission(mission); }

    @Override
    public Mission createMission(Mission mission) { return missionDao.createMission(mission); }

    @Override
    public void deleteMission(String missionNameOrId) { missionDao.deleteMission(missionNameOrId); }

}
