package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoOidcAuthScheme;
import dev.getelements.elements.sdk.model.auth.OidcAuthScheme;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoOidcAuthSchemeMapper extends MapperRegistry.ReversibleMapper<MongoOidcAuthScheme, OidcAuthScheme> {

    @Override
    OidcAuthScheme forward(MongoOidcAuthScheme source);

    @Override
    MongoOidcAuthScheme reverse(OidcAuthScheme source);

}
