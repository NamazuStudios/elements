package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoIosApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.IosApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoIosApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoIosApplicationConfiguration,
        IosApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    IosApplicationConfiguration forward(MongoIosApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoIosApplicationConfiguration reverse(IosApplicationConfiguration source);

}
