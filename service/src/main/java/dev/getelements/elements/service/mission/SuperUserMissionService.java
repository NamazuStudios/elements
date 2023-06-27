package dev.getelements.elements.service.mission;

import dev.getelements.elements.model.mission.Mission;

public class SuperUserMissionService extends AnonMissionService implements MissionService {

    @Override
    public Mission updateMission(Mission mission) { return missionDao.updateMission(mission); }

    @Override
    public Mission createMission(Mission mission) { return missionDao.createMission(mission); }

    @Override
    public void deleteMission(String missionNameOrId) { missionDao.deleteMission(missionNameOrId); }

}
