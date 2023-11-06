package dev.getelements.elements.service.profile;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileImageRequest;

import java.util.UUID;

import static java.util.Arrays.asList;

public class ProfileImageObjectUtils {

    private static final String DEFAULT_IMAGE_PATH = "/user/%s/profile/%s/%s.bin";

    public LargeObject createImageObject(Profile profile) {
        LargeObject largeObject = new LargeObject();
        largeObject.setAccessPermissions(setupDefaultPermissions(profile));
        largeObject.setPath(String.format(DEFAULT_IMAGE_PATH, profile.getUser().getId(), profile.getId(), UUID.randomUUID()));
        return largeObject;
    }

    LargeObject updateProfileImageObject(Profile profile, LargeObject objectToUpdate, UpdateProfileImageRequest updateProfileImageRequest) {
        objectToUpdate.setMimeType(updateProfileImageRequest.getMimeType());
        objectToUpdate.setAccessPermissions(setupDefaultPermissions(profile));
        return objectToUpdate;
    }

    public LargeObjectReference createReference(LargeObject persistedObject) {
        LargeObjectReference reference = new LargeObjectReference();
        reference.setId(persistedObject.getId());
        reference.setMimeType(persistedObject.getMimeType());
        return reference;
    }

    private AccessPermissions setupDefaultPermissions(Profile profile) {
        AccessPermissions imagePermissions = new AccessPermissions();
        imagePermissions.setRead(Subjects.wildcardSubject());
        imagePermissions.setWrite(getSubjectsWithProfileAccess(profile));
        imagePermissions.setDelete(getSubjectsWithProfileAccess(profile));

        return imagePermissions;
    }

    private Subjects getSubjectsWithProfileAccess(Profile profile) {
        Subjects subjects = new Subjects();
        subjects.setWildcard(false);
        subjects.setUsers(asList(profile.getUser()));
        subjects.setProfiles(asList(profile));
        return subjects;
    }
}
