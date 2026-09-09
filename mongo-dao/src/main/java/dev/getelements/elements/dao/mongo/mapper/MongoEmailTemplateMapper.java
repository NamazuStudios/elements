package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.schema.email.MongoEmailTemplate;
import dev.getelements.elements.sdk.model.schema.email.EmailTemplate;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoEmailTemplateMapper extends MapperRegistry.ReversibleMapper<MongoEmailTemplate, EmailTemplate> {

    @Override
    @Mapping(target = "id", source = "objectId")
    EmailTemplate forward(MongoEmailTemplate source);

    @Override
    @InheritInverseConfiguration
    MongoEmailTemplate reverse(EmailTemplate source);

}
