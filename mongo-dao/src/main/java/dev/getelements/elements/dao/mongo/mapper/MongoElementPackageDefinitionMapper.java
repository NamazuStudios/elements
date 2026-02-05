package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.system.MongoElementPackageDefinition;
import dev.getelements.elements.sdk.model.system.ElementPackageDefinition;
import org.mapstruct.Mapper;

@Mapper
public interface MongoElementPackageDefinitionMapper {

    ElementPackageDefinition toModel(MongoElementPackageDefinition mongoElementPackageDefinition);

    MongoElementPackageDefinition toEntity(ElementPackageDefinition elementPackageDefinition);

}