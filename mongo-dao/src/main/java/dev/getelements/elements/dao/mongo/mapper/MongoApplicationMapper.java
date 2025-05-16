package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoApplicationMapper extends MapperRegistry.ReversibleMapper<MongoApplication, Application> {

    @Override
    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "scriptRepoUrl", ignore = true)
    @Mapping(target = "httpDocumentationUrl", ignore = true)
    @Mapping(target = "httpDocumentationUiUrl", ignore = true)
    @Mapping(target = "httpTunnelEndpointUrl", ignore = true)
    @Mapping(target = "applicationConfiguration", ignore = true)
    Application forward(MongoApplication source);

    @Override
    @InheritInverseConfiguration
    MongoApplication reverse(Application source);

}
