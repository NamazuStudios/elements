package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.system.MongoElementPathDefinition;
import dev.getelements.elements.sdk.model.system.ElementPathDefinition;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper
public interface MongoElementDefinitionMapper extends MapperRegistry.ReversibleMapper<MongoElementPathDefinition, ElementPathDefinition> {

    @Override
    ElementPathDefinition forward(MongoElementPathDefinition source);

    @Override
    MongoElementPathDefinition reverse(ElementPathDefinition source);

}
