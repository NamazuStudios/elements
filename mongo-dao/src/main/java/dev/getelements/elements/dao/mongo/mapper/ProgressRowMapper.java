package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.mission.MongoProgress;
import dev.getelements.elements.sdk.model.mission.ProgressRow;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class, MongoDBMapper.class})
public interface ProgressRowMapper extends MapperRegistry.Mapper<MongoProgress, ProgressRow> {

    @Override
    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "profileId", source = "profile.objectId")
    @Mapping(target = "profileImageUrl", source = "profile.imageUrl")
    @Mapping(target = "profileDisplayName", source = "profile.displayName")
    @Mapping(target = "stepDisplayName", source = "currentStep.displayName")
    @Mapping(target = "stepDescription", source = "currentStep.description")
    @Mapping(target = "remaining", source = "remaining")
    @Mapping(target = "stepCount", source = "currentStep.count")
    ProgressRow forward(MongoProgress source);

}
