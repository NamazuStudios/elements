package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.dao.mongo.model.system.MongoElementDeployment;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.bson.types.ObjectId;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class, MongoElementDefinitionMapper.class})
public interface MongoElementDeploymentMapper extends MapperRegistry.ReversibleMapper<MongoElementDeployment, ElementDeployment> {

    @Override
    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "elm", source = "elm")
    ElementDeployment forward(MongoElementDeployment source);

    @Override
    @InheritInverseConfiguration
    MongoElementDeployment reverse(ElementDeployment source);

    default LargeObjectReference toLargeObjectReference(MongoLargeObject source) {
        if (source == null) {
            return null;
        }
        final var ref = new LargeObjectReference();
        ref.setId(source.getId() == null ? null : source.getId().toHexString());
        ref.setUrl(source.getUrl());
        ref.setMimeType(source.getMimeType());
        ref.setState(source.getState());
        ref.setLastModified(source.getLastModified());
        return ref;
    }

    default MongoLargeObject toMongoLargeObject(LargeObjectReference source) {
        if (source == null) {
            return null;
        }
        final var obj = new MongoLargeObject();
        obj.setId(source.getId() == null ? null : new ObjectId(source.getId()));
        obj.setUrl(source.getUrl());
        obj.setMimeType(source.getMimeType());
        obj.setState(source.getState());
        obj.setLastModified(source.getLastModified());
        return obj;
    }

}
