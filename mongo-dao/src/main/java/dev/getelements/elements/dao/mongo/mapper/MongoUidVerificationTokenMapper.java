package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.MongoUidVerificationToken;
import dev.getelements.elements.sdk.model.user.UidVerificationToken;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoUidVerificationTokenMapper extends MapperRegistry.ReversibleMapper<MongoUidVerificationToken, UidVerificationToken> {

    @Override
    @Mapping(target = "user.id", source = "user.objectId")
    UidVerificationToken forward(MongoUidVerificationToken source);

    @Override
    @Mapping(target = "user.objectId", source = "user.id")
    MongoUidVerificationToken reverse(UidVerificationToken source);

}
