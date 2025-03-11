package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.MongoUserUid;
import dev.getelements.elements.sdk.model.user.UserUid;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoUserUidMapper extends MapperRegistry.ReversibleMapper<MongoUserUid, UserUid> {

    @Override
    @Mapping(target = "scheme", source = "id.scheme")
    @Mapping(target = "id", source = "id.id")
    @Mapping(target = "userId", source = "user.objectId")
    UserUid forward(MongoUserUid source);

    @Override
    @Mapping(target = "id.scheme", source = "scheme")
    @Mapping(target = "id.id", source = "id")
    MongoUserUid reverse(UserUid source);
}

