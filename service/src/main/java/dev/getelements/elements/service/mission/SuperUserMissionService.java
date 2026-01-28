package dev.getelements.elements.service.mission;

import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.UpdateMissionRequest;
import dev.getelements.elements.sdk.service.mission.MissionService;
import org.eclipse.jgit.lib.ObjectId;

public class SuperUserMissionService extends AnonMissionService implements MissionService {

    @Override
    public Mission updateMission(final String missionNameOrId, final Mission mission) { return missionDao.updateMission(missionNameOrId, mission); }

    @Override
    public Mission updateMission(final String missionNameOrId, final UpdateMissionRequest request) {

        final var mission = new Mission();

        if(ObjectId.isId(missionNameOrId)) {
            mission.setId(missionNameOrId);
        } else {
            mission.setName(missionNameOrId);
        }

        mission.setDescription(request.getDescription());
        mission.setDisplayName(request.getDisplayName());
        mission.setFinalRepeatStep(request.getFinalRepeatStep());
        mission.setMetadata(request.getMetadata());
        mission.setSteps(request.getSteps());
        mission.setTags(request.getTags());

        return missionDao.updateMission(mission);
    }

    @Override
    public Mission createMission(final Mission mission) { return missionDao.createMission(mission); }

    @Override
    public void deleteMission(final String missionNameOrId) { missionDao.deleteMission(missionNameOrId); }

}
