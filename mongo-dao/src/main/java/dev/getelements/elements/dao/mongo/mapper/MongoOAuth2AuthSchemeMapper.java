package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoOAuth2AuthScheme;
import dev.getelements.elements.sdk.model.auth.OAuth2AuthScheme;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class})
public interface MongoOAuth2AuthSchemeMapper extends MapperRegistry.ReversibleMapper<MongoOAuth2AuthScheme, OAuth2AuthScheme> {

    @Override
    OAuth2AuthScheme forward(MongoOAuth2AuthScheme source);

    @Override
    MongoOAuth2AuthScheme reverse(OAuth2AuthScheme source);

}