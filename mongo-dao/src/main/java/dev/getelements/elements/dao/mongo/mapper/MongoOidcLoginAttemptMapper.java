package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoOidcLoginAttempt;
import dev.getelements.elements.sdk.model.auth.OidcLoginAttempt;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class})
public interface MongoOidcLoginAttemptMapper
        extends MapperRegistry.ReversibleMapper<MongoOidcLoginAttempt, OidcLoginAttempt> {

    @Override
    OidcLoginAttempt forward(MongoOidcLoginAttempt source);

    @Override
    MongoOidcLoginAttempt reverse(OidcLoginAttempt source);

}
