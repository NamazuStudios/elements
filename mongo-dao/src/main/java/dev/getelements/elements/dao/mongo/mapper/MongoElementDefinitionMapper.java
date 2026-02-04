package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.system.MongoElementDefinition;
import dev.getelements.elements.sdk.model.system.ElementDefinition;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper
public interface MongoElementDefinitionMapper extends MapperRegistry.ReversibleMapper<MongoElementDefinition, ElementDefinition> {

    @Override
    ElementDefinition forward(MongoElementDefinition source);

    @Override
    MongoElementDefinition reverse(ElementDefinition source);

}
