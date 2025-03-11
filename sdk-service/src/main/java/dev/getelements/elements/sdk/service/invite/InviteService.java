package dev.getelements.elements.sdk.service.invite;

import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
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
