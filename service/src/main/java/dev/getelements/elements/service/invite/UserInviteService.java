package dev.getelements.elements.service.invite;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.UserDao;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.sdk.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.sdk.model.invite.PhoneMatchedInvitation;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.invite.InviteService;
import dev.getelements.elements.util.PhoneNormalizer;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static java.util.stream.Collectors.toUnmodifiableList;

public class UserInviteService implements InviteService {

    private UserDao userDao;

    private ProfileDao profileDao;

    @Override
    public InviteViaPhonesResponse inviteViaPhoneNumbers(InviteViaPhonesRequest inviteRequest, int offset, int count) {

        List<String> normalizedPhoneList = inviteRequest.getPhoneNumbers().stream()
                .map(PhoneNormalizer::normalizePhoneNb)
                .filter(Optional::isPresent)
                .map(Optional::get)
                .collect(toUnmodifiableList());

        List<PhoneMatchedInvitation> invitations = new ArrayList<>();

        getUserDao().getUsersByPrimaryPhoneNumbers(0, USERS_PER_PHONE_LIMIT, normalizedPhoneList)
                .forEach(matchedUser -> invitations.add(createInvitation(matchedUser)));

        InviteViaPhonesResponse response = new InviteViaPhonesResponse();
        response.setMatched(invitations);
        return response;
    }

    private PhoneMatchedInvitation createInvitation(User user) {
        PhoneMatchedInvitation invitation = new PhoneMatchedInvitation();
        invitation.setPhoneNumber(user.getPrimaryPhoneNb());

        List<String> profileIds = getProfileDao()
                .getActiveProfiles(0, PROFILES_PER_USER_LIMIT, null, user.getId(), null, null)
                .getObjects().stream().map(Profile::getId).collect(toUnmodifiableList());
        invitation.setProfileIds(profileIds);

        return invitation;
    }

    public UserDao getUserDao() {
        return userDao;
    }

    @Inject
    public void setUserDao(UserDao userDao) {
        this.userDao = userDao;
    }

    public ProfileDao getProfileDao() {
        return profileDao;
    }

    @Inject
    public void setProfileDao(ProfileDao profileDao) {
        this.profileDao = profileDao;
    }
}
