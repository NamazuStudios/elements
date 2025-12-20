package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoOculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.OculusApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoOculusApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoOculusApplicationConfiguration,
        OculusApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    OculusApplicationConfiguration forward(MongoOculusApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoOculusApplicationConfiguration reverse(OculusApplicationConfiguration source);

}
