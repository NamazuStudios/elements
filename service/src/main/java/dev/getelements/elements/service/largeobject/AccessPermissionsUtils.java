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
        return subjects.getUsers().stream()
                .anyMatch(user -> current.getId().equals(user.getId()));
    }

    public boolean hasProfileAccess(final Subjects subjects) {
        //profile is required, so getCurrentProfile is right one
        final var current = getProfileService().getCurrentProfile();
        return subjects.getProfiles().stream()
                .anyMatch(profile -> current.getId().equals(profile.getId()));
    }

    public boolean hasReadAccess(final AccessPermissions accessPermissions) {
        Subjects readSubjects = accessPermissions.getRead();
        return readSubjects.isWildcard() || hasUserAccess(readSubjects) || hasProfileAccess(readSubjects);
    }

    public boolean hasWriteAccess(final AccessPermissions accessPermissions) {
        Subjects writeSubjects = accessPermissions.getWrite();
        return writeSubjects.isWildcard() || hasUserAccess(writeSubjects) || hasProfileAccess(writeSubjects);
    }

    public boolean hasDeleteAccess(final AccessPermissions accessPermissions) {
        Subjects deleteSubjects = accessPermissions.getDelete();
        return deleteSubjects.isWildcard() || hasUserAccess(deleteSubjects) || hasProfileAccess(deleteSubjects);
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
