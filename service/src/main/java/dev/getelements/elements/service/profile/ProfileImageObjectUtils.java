package dev.getelements.elements.service.profile;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.LargeObjectService;

import javax.inject.Inject;
import java.io.IOException;
import java.util.UUID;

import static java.util.Arrays.asList;
import static java.util.Objects.isNull;

public class ProfileImageObjectUtils {

    private static final String DEFAULT_IMAGE_PATH = "/user/%s/profile/%s/%s.bin";
    private static final String DEFAULT_MIME_TYPE = "image/png";

    private LargeObjectService largeObjectService;
    
    void createProfileImageObject(Profile profile, LargeObjectReference imageObjectReference) {
        if (isNull(imageObjectReference)) {
            imageObjectReference = new LargeObjectReference();
        }
        LargeObject imageObject = createImageObjectFromReference(profile, imageObjectReference);
        LargeObject persistedImageObject = largeObjectService.saveOrUpdateLargeObject(imageObject);
        imageObjectReference.setId(persistedImageObject.getId());

        profile.setImageUrl(imageObjectReference.getUrl());
        profile.setImageObject(imageObjectReference);
    }

    //TODO: update does not work currently due to mongo object ID implementation. Need to create new & delete old object
    void updateProfileImageObject(Profile profile, LargeObjectReference imageObjectReference) throws IOException {
        String currentImageId = profile.getImageObject().getId();
        LargeObject newImageObject = createImageObjectFromReference(profile, imageObjectReference);

        //TODO: check if this two lines are in one transaction
        LargeObject persistedObject = largeObjectService.saveOrUpdateLargeObject(newImageObject);
        largeObjectService.deleteLargeObject(currentImageId);

        imageObjectReference.setId(persistedObject.getId());

        profile.setImageUrl(imageObjectReference.getUrl());
        profile.setImageObject(imageObjectReference);
    }

    private LargeObject createImageObjectFromReference(Profile profile, LargeObjectReference reference) {
        LargeObject largeObject = new LargeObject();
        
        largeObject.setAccessPermissions(setupDefaultPermissions(profile));
        largeObject.setMimeType(isNull(reference.getMimeType()) ? DEFAULT_MIME_TYPE : reference.getMimeType());
        largeObject.setUrl(reference.getUrl());
        largeObject.setPath(String.format(DEFAULT_IMAGE_PATH, profile.getUser().getId(), profile.getId(), UUID.randomUUID()));

        return largeObject;
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


    public LargeObjectService getLargeObjectService() {
        return largeObjectService;
    }

    @Inject
    public void setLargeObjectService(LargeObjectService largeObjectService) {
        this.largeObjectService = largeObjectService;
    }
}
