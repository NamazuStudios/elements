package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoTotpConfiguration;
import dev.getelements.elements.sdk.model.auth.TotpConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class})
public interface MongoTotpConfigurationMapper
        extends MapperRegistry.ReversibleMapper<MongoTotpConfiguration, TotpConfiguration> {

    @Override
    TotpConfiguration forward(MongoTotpConfiguration source);

    @Override
    MongoTotpConfiguration reverse(TotpConfiguration source);

}
