package dev.getelements.elements.service;

import dev.getelements.elements.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.rt.annotation.DeprecationDefinition;
import dev.getelements.elements.rt.annotation.Expose;
import dev.getelements.elements.rt.annotation.ModuleDefinition;

@Expose({
        @ModuleDefinition(
                value = "eci.elements.service.invite"
        ),
        @ModuleDefinition(
                value = "namazu.elements.service.invite",
                deprecated = @DeprecationDefinition("Use eci.elements.service.invite instead.")
        )
})
public interface InviteService {

    int PROFILES_PER_USER_LIMIT = 5;
    int USERS_PER_PHONE_LIMIT = 5;

    /**
     * Get an {@link InviteViaPhonesResponse} by invite request data with phone number list
     *
     * @param inviteRequest the request
     * @return the {@link InviteViaPhonesResponse}
     */
    InviteViaPhonesResponse inviteViaPhoneNumbers(InviteViaPhonesRequest inviteRequest, int offset, int count);

}
