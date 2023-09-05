package dev.getelements.elements.rest;

import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.SubjectRequest;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;

class LargeObjectRequestFactory {

    static final String DEFAULT_MIME_TYPE = "mime";

    CreateLargeObjectRequest createRequestWithAccess(boolean read, boolean write, boolean delete) {
        CreateLargeObjectRequest result = new CreateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(requestWithAccess(read));
        result.setWrite(requestWithAccess(write));
        result.setDelete(requestWithAccess(delete));

        return result;
    }

    UpdateLargeObjectRequest updateLargeObjectRequest(boolean read, boolean write, boolean delete) {
        UpdateLargeObjectRequest result = new UpdateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(requestWithAccess(read));
        result.setWrite(requestWithAccess(write));
        result.setDelete(requestWithAccess(delete));

        return result;
    }

    CreateLargeObjectRequest createRequestWithFullAccess() {
        return createRequestWithAccess(true, true, true);
    }

    UpdateLargeObjectRequest updateRequestWithFullAccess() {
        return updateLargeObjectRequest(true, true, true);
    }

    SubjectRequest requestWithAccess(boolean wildcard) {
        SubjectRequest subjectRequest = SubjectRequest.newDefaultRequest();
        subjectRequest.setWildcard(wildcard);
        return subjectRequest;
    }
}
