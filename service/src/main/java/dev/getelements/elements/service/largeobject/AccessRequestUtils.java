package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.ProfileDao;
import dev.getelements.elements.dao.UserDao;
import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.Subjects;

import javax.inject.Inject;

import static java.util.stream.Collectors.toList;

public class AccessRequestUtils {

    private UserDao userDao;

    private ProfileDao profileDao;

    public AccessPermissions createAccessPermissions(
            final SubjectRequest readRequest,
            final SubjectRequest writeRequest,
            final SubjectRequest deleteRequest) {
        final var accessPermissions = new AccessPermissions();
        accessPermissions.setRead(fromRequest(readRequest));
        accessPermissions.setWrite(fromRequest(writeRequest));
        accessPermissions.setDelete(fromRequest(deleteRequest));
        return accessPermissions;
    }

    public Subjects fromRequest(final SubjectRequest subjectRequest) {
        Subjects subjects = new Subjects();
        subjects.setWildcard(subjectRequest.isWildcard());
        subjects.setUsers(subjectRequest.getUserIds().stream().map(getUserDao()::getActiveUser).collect(toList()));
        subjects.setProfiles(subjectRequest.getProfileIds().stream().map(getProfileDao()::getActiveProfile).collect(toList()));
        return subjects;
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
