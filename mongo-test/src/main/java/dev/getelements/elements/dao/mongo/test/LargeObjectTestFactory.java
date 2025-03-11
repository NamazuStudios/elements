package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.Subjects;

import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.model.largeobject.Subjects.wildcardSubject;
import static java.util.Collections.emptyList;

public class LargeObjectTestFactory {

    private UserTestFactory userTestFactory;

    private static final String DEFAULT_MIME_TYPE = "testMime";
    private static final String TEST_URL = "/url/lo";
    private static final String TEST_PATH = "/path/img";

    public LargeObject createDefaultLargeObject(AccessPermissions accessPermissions) {
        LargeObject largeObject = new LargeObject();
        largeObject.setMimeType(DEFAULT_MIME_TYPE);
        largeObject.setUrl(TEST_URL);
        largeObject.setPath(TEST_PATH);
        largeObject.setAccessPermissions(accessPermissions);

        return largeObject;
    }

    public AccessPermissions wildcardAccess() {
        AccessPermissions accessPermissions = new AccessPermissions();
        accessPermissions.setWrite(wildcardSubject());
        accessPermissions.setRead(wildcardSubject());
        accessPermissions.setDelete(wildcardSubject());

        return accessPermissions;
    }

    public AccessPermissions notWildcardReadAccess() {
        AccessPermissions accessPermissions = new AccessPermissions();

        Subjects readSubjects = new Subjects();
        readSubjects.setWildcard(false);
        readSubjects.setUsers(emptyList());
        readSubjects.setProfiles(emptyList());

        accessPermissions.setRead(readSubjects);
        accessPermissions.setWrite(wildcardSubject());
        accessPermissions.setDelete(wildcardSubject());

        return accessPermissions;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
