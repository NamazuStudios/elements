package dev.getelements.elements.service.util;

import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.CreateApplicationRequest;
import dev.getelements.elements.sdk.model.session.Session;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.user.UserCreateResponse;
import dev.getelements.elements.sdk.annotation.ElementPrivate;
import org.mapstruct.Mapper;

@Mapper
@ElementPrivate
public interface ServicesMapper {

    User toUser(User source);

    Session toSession(Session source);

    UserCreateResponse toUserCreateResponse(User user);

    CreateApplicationRequest toCreateApplicationRequest(Application application);

    Application toApplication(CreateApplicationRequest createApplicationRequest);
}
