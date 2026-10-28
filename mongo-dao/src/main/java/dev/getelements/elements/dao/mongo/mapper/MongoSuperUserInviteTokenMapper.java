package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.invite.MongoSuperUserInviteToken;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoSuperUserInviteTokenMapper extends MapperRegistry.ReversibleMapper<MongoSuperUserInviteToken, SuperUserInvite> {

    @Override
    @Mapping(target = "token", source = "token")
    @Mapping(target = "invitedEmail", source = "invitedEmail")
    @Mapping(target = "invitedBy.id", source = "invitedBy.objectId")
    @Mapping(target = "status", source = "status")
    SuperUserInvite forward(MongoSuperUserInviteToken source);

    @Override
    @Mapping(target = "token", source = "token")
    @Mapping(target = "invitedEmail", source = "invitedEmail")
    @Mapping(target = "invitedBy.objectId", source = "invitedBy.id")
    @Mapping(target = "status", source = "status")
    MongoSuperUserInviteToken reverse(SuperUserInvite source);
}
