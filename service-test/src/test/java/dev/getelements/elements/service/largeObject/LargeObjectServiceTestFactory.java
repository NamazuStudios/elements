package dev.getelements.elements.service.largeObject;

import dev.getelements.elements.model.largeobject.*;

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

    CreateLargeObjectRequest defaultCreateRequestWithWildcardAccess(boolean read, boolean write, boolean delete) {
        CreateLargeObjectRequest result = new CreateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(defaultRequestWithWildcardAccess(read));
        result.setWrite(defaultRequestWithWildcardAccess(write));
        result.setDelete(defaultRequestWithWildcardAccess(delete));

        return result;
    }

    SubjectRequest defaultRequestWithWildcardAccess(boolean wildcard) {
        SubjectRequest subjectRequest = SubjectRequest.newDefaultRequest();
        subjectRequest.setWildcard(wildcard);
        return subjectRequest;
    }

    LargeObject wildcardLargeObject() {
        return defaultLargeObjectWithWildcardAccess(true, true, true);
    }

    LargeObject defaultLargeObjectWithWildcardAccess(boolean read, boolean write, boolean delete) {
        LargeObject result = new LargeObject();
        result.setId(TEST_ID);
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setAccessPermissions(defaultAccessPermission(read, write, delete));

        return result;
    }

    AccessPermissions defaultAccessPermission(boolean read, boolean write, boolean delete) {
        AccessPermissions result = new AccessPermissions();
        result.setRead(defaultSubjectWithWildcardAccess(read));
        result.setWrite(defaultSubjectWithWildcardAccess(write));
        result.setDelete(defaultSubjectWithWildcardAccess(delete));

        return result;
    }

    Subjects defaultSubjectWithWildcardAccess(boolean wildcard) {
        Subjects subjects = Subjects.wildcardSubject();
        subjects.setWildcard(wildcard);
        return subjects;
    }
}
