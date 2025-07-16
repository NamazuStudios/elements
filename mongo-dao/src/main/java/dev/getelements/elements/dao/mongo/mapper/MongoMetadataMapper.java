package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.metadata.MongoMetadata;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoMetadataMapper extends MapperRegistry.ReversibleMapper<
        MongoMetadata,
        Metadata> {

    @Override
    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "spec.id", source = "spec.objectId")
    Metadata forward(MongoMetadata source);

    @Override
    @InheritInverseConfiguration
    MongoMetadata reverse(Metadata source);

}
