package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoMatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.MatchmakingApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoMatchmakingApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoMatchmakingApplicationConfiguration,
        MatchmakingApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    MatchmakingApplicationConfiguration forward(MongoMatchmakingApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoMatchmakingApplicationConfiguration reverse(MatchmakingApplicationConfiguration source);

}
