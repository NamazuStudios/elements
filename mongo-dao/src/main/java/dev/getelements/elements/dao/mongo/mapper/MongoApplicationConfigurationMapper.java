package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoApplicationConfiguration,
        ApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    ApplicationConfiguration forward(MongoApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoApplicationConfiguration reverse(ApplicationConfiguration source);

}
