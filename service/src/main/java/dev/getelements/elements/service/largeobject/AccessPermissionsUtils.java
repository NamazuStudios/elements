package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import java.util.Optional;

public class AccessPermissionsUtils {

    private User uesr;

    private Optional<Profile> profileOptional;

    public boolean hasUserAccess(final Subjects subjects) {
        return subjects.getUsers().stream()
                .anyMatch(user -> getUesr().getId().equals(user.getId()));
    }

    public boolean hasProfileAccess(final Subjects subjects) {
        return getProfileOptional().filter(value -> subjects.getProfiles().stream()
                .anyMatch(profile -> value.getId().equals(profile.getId()))).isPresent();
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

    public User getUesr() {
        return uesr;
    }

    @Inject
    public void setUesr(User uesr) {
        this.uesr = uesr;
    }

    public Optional<Profile> getProfileOptional() {
        return profileOptional;
    }

    @Inject
    public void setProfileOptional(Optional<Profile> profileOptional) {
        this.profileOptional = profileOptional;
    }

}
