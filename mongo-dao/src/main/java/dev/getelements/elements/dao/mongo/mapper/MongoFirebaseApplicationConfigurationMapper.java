package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoFirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.FirebaseApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoFirebaseApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoFirebaseApplicationConfiguration,
        FirebaseApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    FirebaseApplicationConfiguration forward(MongoFirebaseApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoFirebaseApplicationConfiguration reverse(FirebaseApplicationConfiguration source);

}
