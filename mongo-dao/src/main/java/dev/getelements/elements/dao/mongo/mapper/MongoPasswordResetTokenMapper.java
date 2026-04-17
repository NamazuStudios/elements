package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.user.MongoPasswordResetToken;
import dev.getelements.elements.sdk.model.user.PasswordResetToken;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoPasswordResetTokenMapper extends MapperRegistry.ReversibleMapper<MongoPasswordResetToken, PasswordResetToken> {

    @Override
    @Mapping(target = "id", source = "token")
    @Mapping(target = "user.id", source = "user.objectId")
    PasswordResetToken forward(MongoPasswordResetToken source);

    @Override
    @Mapping(target = "token", source = "id")
    @Mapping(target = "user.objectId", source = "user.id")
    MongoPasswordResetToken reverse(PasswordResetToken source);

}
