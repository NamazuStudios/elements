package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoSteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.SteamApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoSteamApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoSteamApplicationConfiguration,
        SteamApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    SteamApplicationConfiguration forward(MongoSteamApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoSteamApplicationConfiguration reverse(SteamApplicationConfiguration source);

}
