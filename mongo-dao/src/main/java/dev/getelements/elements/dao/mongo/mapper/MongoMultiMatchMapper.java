package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.match.MongoMultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.Mappings;

@Mapper(uses = {
        PropertyConverters.class,
        MongoUniqueCodeMapper.class,
        MongoApplicationMapper.class,
        MongoMatchmakingApplicationConfigurationMapper.class
})
public interface MongoMultiMatchMapper extends MapperRegistry.ReversibleMapper<MongoMultiMatch, MultiMatch> {

    @Override
    MongoMultiMatch reverse(MultiMatch source);

    @Override
    @Mappings(
            @Mapping(
                target = "count",
                expression = "java(source.getProfiles() == null ? 0 : source.getProfiles().size())"
            )
    )
    MultiMatch forward(MongoMultiMatch source);

}
