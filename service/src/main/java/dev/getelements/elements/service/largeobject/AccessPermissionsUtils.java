package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.service.ProfileService;
import dev.getelements.elements.service.UserService;

import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

public class AccessPermissionsUtils {

    private UserService userService;

    private ProfileService profileService;

    public Subjects fromRequest(final SubjectRequest subjectRequest) {
        Subjects subjects = new Subjects();
        subjects.setWildcard(subjectRequest.isWildcard());
        subjects.setUsers(subjectRequest.getUserIds().stream().map(userService::getUser).collect(toList()));
        subjects.setProfiles(subjectRequest.getProfileIds().stream().map(profileService::getProfile).collect(toList()));
        return subjects;
    }

    public boolean hasUserAccess(final Subjects subjects) {
        final var current = getUserService().getCurrentUser();
        return subjects.isWildcard() || subjects
                .getUsers()
                .stream()
                .anyMatch(user -> current.getId().equals(user.getId()));
    }

    public boolean hasProfileAccess(final Subjects subjects) {
        final var current = getProfileService().findCurrentProfile();
        return subjects.isWildcard() ||
                // TODO: Please check this logic here.
                // TODO: I don't think this is right. If currently we have no profile then the request should not pass.
                current.isEmpty() ||
                subjects.getProfiles()
                .stream()
                .anyMatch(profile -> current.get().getId().equals(profile.getId()));
//        return subjects.isWildcard() || getProfileService()
//                .findCurrentProfile()
//                .map(current -> subjects.getProfiles()
//                    .stream()
//                    .anyMatch(profile -> current.getId().equals(profile.getId()))
//                ).orElse(false);
    }

    public boolean hasReadAccess(final AccessPermissions accessPermissions) {
        return hasUserAccess(accessPermissions.getRead()) || hasProfileAccess(accessPermissions.getRead());
    }

    public boolean hasWriteAccess(final AccessPermissions accessPermissions) {
        return hasUserAccess(accessPermissions.getWrite()) || hasProfileAccess(accessPermissions.getWrite());
    }

    public boolean hasDeleteAccess(final AccessPermissions accessPermissions) {
        return hasUserAccess(accessPermissions.getDelete()) || hasProfileAccess(accessPermissions.getDelete());
    }

    ProfileService getProfileService() {
        return profileService;
    }

    @Inject
    public void setProfileService(ProfileService profileService) {
        this.profileService = profileService;
    }

    public UserService getUserService() {
        return userService;
    }

    @Inject
    public void setUserService(UserService userService) {
        this.userService = userService;
    }

}
