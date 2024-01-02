package dev.getelements.elements.service.invite;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.invite.InviteViaPhonesRequest;
import dev.getelements.elements.model.invite.InviteViaPhonesResponse;
import dev.getelements.elements.model.invite.PhoneMatchedInvitation;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.service.InviteService;
import dev.getelements.elements.util.PhoneNormalizer;

import javax.inject.Inject;
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

        normalizedPhoneList.forEach(phone -> getUserDao()
                .getActiveUsersByPrimaryPhoneNb(0, USERS_PER_PHONE_LIMIT, phone)
                .forEach(matchedUser -> invitations.add(createInvitation(matchedUser))
                ));

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
