package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoGooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.GooglePlayApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoGooglePlayApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoGooglePlayApplicationConfiguration,
        GooglePlayApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    GooglePlayApplicationConfiguration forward(MongoGooglePlayApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoGooglePlayApplicationConfiguration reverse(GooglePlayApplicationConfiguration source);

}
