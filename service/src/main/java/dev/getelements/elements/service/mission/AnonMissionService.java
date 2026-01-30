package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.dao.MissionDao;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.mission.CreateMissionRequest;
import dev.getelements.elements.sdk.model.mission.UpdateMissionRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.mission.Mission;

import dev.getelements.elements.sdk.service.mission.MissionService;
import jakarta.inject.Inject;
import java.util.List;

public class AnonMissionService implements MissionService {

    protected User user;

    protected MissionDao missionDao;

    @Override
    public Mission getMissionByNameOrId(String missionNameOrId) { return missionDao.getMissionByNameOrId(missionNameOrId); }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, List<String> tags) { return missionDao.getMissions(offset,count, tags); }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, String query)  { return missionDao.getMissions(offset, count, query); }

    @Override
    public Mission updateMission(String missionNameOrId, Mission mission) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }

    @Override
    public Mission updateMission(String missionNameOrId, UpdateMissionRequest mission) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }

    @Override
    public Mission createMission(CreateMissionRequest request) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }

    @Override
    public void deleteMission(String missionNameOrId) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }


    public MissionDao setMissionDao() {
        return missionDao;
    }

    @Inject
    public void getMissionDao(MissionDao missionDao) {
        this.missionDao = missionDao;
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }
}
