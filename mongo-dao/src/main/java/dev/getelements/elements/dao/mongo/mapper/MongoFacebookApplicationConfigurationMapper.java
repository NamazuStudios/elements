package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoFacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FacebookApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoFacebookApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoFacebookApplicationConfiguration,
        FacebookApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    FacebookApplicationConfiguration forward(MongoFacebookApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoFacebookApplicationConfiguration reverse(FacebookApplicationConfiguration source);

}
