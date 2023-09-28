package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import java.util.Optional;

import static java.util.stream.Collectors.toList;

public class AccessPermissionsUtils {

    private User uesr;

    private UserDao userDao;

    private ProfileDao profileDao;

    private Optional<Profile> profileOptional;

    public Subjects fromRequest(final SubjectRequest subjectRequest) {
        Subjects subjects = new Subjects();
        subjects.setWildcard(subjectRequest.isWildcard());
        subjects.setUsers(subjectRequest.getUserIds().stream().map(getUserDao()::getActiveUser).collect(toList()));
        subjects.setProfiles(subjectRequest.getProfileIds().stream().map(getProfileDao()::getActiveProfile).collect(toList()));
        return subjects;
    }

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

    public Optional<Profile> getProfileOptional() {
        return profileOptional;
    }

    @Inject
    public void setProfileOptional(Optional<Profile> profileOptional) {
        this.profileOptional = profileOptional;
    }

}
