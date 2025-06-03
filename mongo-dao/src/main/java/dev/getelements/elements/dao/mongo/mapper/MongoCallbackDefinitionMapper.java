package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.MongoCallbackDefinition;
import dev.getelements.elements.sdk.model.application.CallbackDefinition;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = MongoElementServiceReferenceMapper.class)
public interface MongoCallbackDefinitionMapper extends MapperRegistry.ReversibleMapper<
        MongoCallbackDefinition,
        CallbackDefinition> {

    @Override
    CallbackDefinition forward(MongoCallbackDefinition source);

    @Override
    MongoCallbackDefinition reverse(CallbackDefinition source);

}
