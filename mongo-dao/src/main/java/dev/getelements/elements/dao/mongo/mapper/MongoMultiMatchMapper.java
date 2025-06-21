package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.match.MongoMultiMatch;
import dev.getelements.elements.sdk.model.match.MultiMatch;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {
        PropertyConverters.class,
        MongoApplicationMapper.class,
        MongoMatchmakingApplicationConfigurationMapper.class
})
public interface MongoMultiMatchMapper extends MapperRegistry.ReversibleMapper<MongoMultiMatch, MultiMatch> {

    @Override
    MongoMultiMatch reverse(MultiMatch source);

    @Override
    MultiMatch forward(MongoMultiMatch source);

}
