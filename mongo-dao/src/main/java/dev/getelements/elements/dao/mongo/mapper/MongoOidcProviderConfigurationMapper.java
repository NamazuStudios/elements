package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoOidcProviderConfiguration;
import dev.getelements.elements.sdk.model.auth.OidcProviderConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class})
public interface MongoOidcProviderConfigurationMapper
        extends MapperRegistry.ReversibleMapper<MongoOidcProviderConfiguration, OidcProviderConfiguration> {

    @Override
    OidcProviderConfiguration forward(MongoOidcProviderConfiguration source);

    @Override
    MongoOidcProviderConfiguration reverse(OidcProviderConfiguration source);

}
