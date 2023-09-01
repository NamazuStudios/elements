package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.model.largeobject.*;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;

import java.util.List;

public class LargeObjectServiceTestFactory {

    static final String TEST_ID = "testId";
    private static final String DEFAULT_MIME_TYPE = "mime";

    UpdateLargeObjectRequest defaultUpdateRequestWithWildcardAccess(boolean read, boolean write, boolean delete) {
        UpdateLargeObjectRequest result = new UpdateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(defaultRequestWithWildcardAccess(read));
        result.setWrite(defaultRequestWithWildcardAccess(write));
        result.setDelete(defaultRequestWithWildcardAccess(delete));

        return result;
    }

    UpdateLargeObjectRequest updateRequestWithFullAccess() {
        return defaultUpdateRequestWithWildcardAccess(true, true, true);
    }

    CreateLargeObjectRequest defaultCreateRequestWithWildcardAccess(boolean read, boolean write, boolean delete) {
        CreateLargeObjectRequest result = new CreateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(defaultRequestWithWildcardAccess(read));
        result.setWrite(defaultRequestWithWildcardAccess(write));
        result.setDelete(defaultRequestWithWildcardAccess(delete));

        return result;
    }

    CreateLargeObjectRequest createRequestWithFullAccess() {
        return defaultCreateRequestWithWildcardAccess(true, true, true);
    }

    SubjectRequest defaultRequestWithWildcardAccess(boolean wildcard) {
        SubjectRequest subjectRequest = SubjectRequest.newDefaultRequest();
        subjectRequest.setWildcard(wildcard);
        return subjectRequest;
    }

    SubjectRequest subjectRequestWithUsersAndProfiles(List<String> userIds, List<String> profileIds) {
        SubjectRequest subjectRequest = defaultRequestWithWildcardAccess(false);
        subjectRequest.setUserIds(userIds);
        subjectRequest.setProfileIds(profileIds);
        return subjectRequest;
    }

    LargeObject defaultLargeObject() {
        LargeObject result = new LargeObject();
        result.setId(TEST_ID);
        result.setMimeType(DEFAULT_MIME_TYPE);

        return result;
    }

    LargeObject wildcardLargeObject() {
        return defaultLargeObjectWithAccess(true, true, true);
    }

    LargeObject defaultLargeObjectWithAccess(boolean read, boolean write, boolean delete) {
        LargeObject result = defaultLargeObject();
        result.setAccessPermissions(defaultAccessPermission(read, write, delete));

        return result;
    }

    LargeObject largeObjectWithUsersAccess(List<User> readUsers, List<User> writeUsers, List<User> deleteUsers) {
        LargeObject result = defaultLargeObject();
        result.setAccessPermissions(accessPermissionForUsers(readUsers, writeUsers, deleteUsers));

        return result;
    }

    LargeObject largeObjectWithProfilesAccess(List<Profile> readProfiles, List<Profile> writeProfiles, List<Profile> deleteProfiles) {
        LargeObject result = defaultLargeObject();
        result.setAccessPermissions(accessPermissionForProfiles(readProfiles, writeProfiles, deleteProfiles));

        return result;
    }

    AccessPermissions defaultAccessPermission(boolean read, boolean write, boolean delete) {
        AccessPermissions result = new AccessPermissions();
        result.setRead(defaultSubjectWithWildcardAccess(read));
        result.setWrite(defaultSubjectWithWildcardAccess(write));
        result.setDelete(defaultSubjectWithWildcardAccess(delete));

        return result;
    }

    AccessPermissions accessPermissionForUsers(List<User> readUsers, List<User> writeUsers, List<User> deleteUsers) {
        AccessPermissions result = new AccessPermissions();
        result.setRead(subjectWithUserAccess(readUsers));
        result.setWrite(subjectWithUserAccess(writeUsers));
        result.setDelete(subjectWithUserAccess(deleteUsers));

        return result;
    }

    AccessPermissions accessPermissionForProfiles(List<Profile> readProfiles, List<Profile> writeProfiles, List<Profile> deleteProfiles) {
        AccessPermissions result = new AccessPermissions();
        result.setRead(subjectWithProfileAccess(readProfiles));
        result.setWrite(subjectWithProfileAccess(writeProfiles));
        result.setDelete(subjectWithProfileAccess(deleteProfiles));

        return result;
    }

    Subjects defaultSubjectWithWildcardAccess(boolean wildcard) {
        Subjects subjects = Subjects.wildcardSubject();
        subjects.setWildcard(wildcard);
        return subjects;
    }

    Subjects subjectWithUserAccess(List<User> users) {
        Subjects subjects = defaultSubjectWithWildcardAccess(false);
        subjects.setUsers(users);
        return subjects;
    }

    Subjects subjectWithProfileAccess(List<Profile> profiles) {
        Subjects subjects = defaultSubjectWithWildcardAccess(false);
        subjects.setProfiles(profiles);
        return subjects;
    }
}
