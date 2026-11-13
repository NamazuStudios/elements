package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.annotation.ElementPrivate;
import dev.getelements.elements.sdk.model.mission.Mission;
import dev.getelements.elements.sdk.model.mission.ProgressMissionInfo;
import org.mapstruct.Mapper;

/**
 * Maps a {@link Mission} into the {@link ProgressMissionInfo} embedded within a progress record
 * when a user starts a mission ({@code StandardAdvancementService}). The two models share the same
 * {@code Step} type, so all properties map by name directly.
 */
@Mapper
@ElementPrivate
public interface ProgressMissionInfoMapper {

    ProgressMissionInfo toProgressMissionInfo(Mission mission);

}
