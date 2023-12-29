package dev.getelements.elements.service.invite;

import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.service.InviteService;

public class AnonInviteService implements InviteService {

    @Override
    public InviteViaPhonesResponse inviteViaPhoneNumbers(InviteViaPhonesRequest inviteRequest, int offset, int count) {
        throw new ForbiddenException();
    }
}
