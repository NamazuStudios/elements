package com.namazustudios.socialengine.service.mission;

import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.exception.ForbiddenException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.mission.Mission;

import javax.inject.Inject;
import java.util.Set;

public class AnonMissionService implements MissionService {

    protected User user;

    protected MissionDao missionDao;

    @Override
    public Mission getMissionByNameOrId(String missionNameOrId) { return missionDao.getMissionByNameOrId(missionNameOrId); }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, Set<String> tags) { return missionDao.getMissions(offset,count, tags); }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, String query)  { return missionDao.getMissions(offset, count, query); }

    @Override
    public Mission updateMission(Mission mission) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }

    @Override
    public Mission createMission(Mission mission) { throw new ForbiddenException("Unprivileged requests are unable to modify missions."); }

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
