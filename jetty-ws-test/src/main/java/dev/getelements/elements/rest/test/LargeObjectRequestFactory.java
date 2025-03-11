package dev.getelements.elements.rest.test;

import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.SubjectRequest;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;

import java.util.List;

class LargeObjectRequestFactory {

    static final String DEFAULT_MIME_TYPE = "mime";
    static final String RANDOM_PUBLIC_URL = "https://cdn.wheel-size.com/automobile/body/chevrolet-astro-1985-1994-1625648125.492117.jpg";

    CreateLargeObjectRequest createRequestWithAccess(boolean read, boolean write, boolean delete) {
        CreateLargeObjectRequest result = new CreateLargeObjectRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(requestWithAccess(read));
        result.setWrite(requestWithAccess(write));
        result.setDelete(requestWithAccess(delete));

        return result;
    }

    CreateLargeObjectRequest createRequestWithUserAccess(List<String> readUserIds, List<String> writeUserIds, List<String> deleteUserIds) {
        CreateLargeObjectRequest result = createRequestWithAccess(false, false, false);
        result.setRead(requestWithUserAccess(readUserIds));
        result.setWrite(requestWithUserAccess(writeUserIds));
        result.setDelete(requestWithUserAccess(deleteUserIds));

        return result;
    }

    CreateLargeObjectRequest createRequestFromUrlWithAccess(boolean read, boolean write, boolean delete) {
        CreateLargeObjectFromUrlRequest result = new CreateLargeObjectFromUrlRequest();
        result.setMimeType(DEFAULT_MIME_TYPE);
        result.setRead(requestWithAccess(read));
        result.setWrite(requestWithAccess(write));
        result.setDelete(requestWithAccess(delete));
        result.setFileUrl(RANDOM_PUBLIC_URL);

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

    SubjectRequest requestWithUserAccess(List<String> users) {
        SubjectRequest subjectRequest = requestWithAccess(false);
        subjectRequest.setUserIds(users);
        return subjectRequest;
    }
}
