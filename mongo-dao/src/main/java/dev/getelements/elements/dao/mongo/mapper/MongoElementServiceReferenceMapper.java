package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoElementServiceReference;
import dev.getelements.elements.sdk.model.application.ElementServiceReference;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper
public interface MongoElementServiceReferenceMapper extends MapperRegistry.ReversibleMapper<
        MongoElementServiceReference,
        ElementServiceReference> {

    @Override
    MongoElementServiceReference reverse(ElementServiceReference source);

    @Override
    ElementServiceReference forward(MongoElementServiceReference source);

}
