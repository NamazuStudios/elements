package dev.getelements.elements.service.profile;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.profile.UpdateProfileImageRequest;
import dev.getelements.elements.service.LargeObjectService;

import javax.inject.Inject;

import java.io.IOException;
import java.util.UUID;

import static java.util.Arrays.asList;

public class ProfileImageObjectUtils {

    private static final String DEFAULT_IMAGE_PATH = "/user/%s/profile/%s/%s.bin";

    private LargeObjectService largeObjectService;
    
    LargeObject createProfileImageObject(Profile profile) {
        LargeObject largeObject = new LargeObject();

        largeObject.setAccessPermissions(setupDefaultPermissions(profile));

        //TODO: should profile request provide this info?
        largeObject.setMimeType("image/png");
        largeObject.setPath(String.format(DEFAULT_IMAGE_PATH, profile.getUser().getId(), profile.getId(), UUID.randomUUID()));

        return largeObjectService.saveOrUpdateLargeObject(largeObject);
    }

    //TODO: update does not work currently due to mongo object ID implementation. Need to create new & delete old object
    LargeObject updateProfileImageObject(Profile profile, UpdateProfileImageRequest request) throws IOException {
        LargeObject largeObject = new LargeObject();

        largeObject.setAccessPermissions(setupDefaultPermissions(profile));
        largeObject.setMimeType(request.getMimeType());
        largeObject.setPath(String.format(DEFAULT_IMAGE_PATH, profile.getUser().getId(), profile.getId(), UUID.randomUUID()));
        largeObject.setUrl(request.getImageObject().getUrl());

        //TODO: check if this two lines are in one transaction
        LargeObject persistedObject = largeObjectService.saveOrUpdateLargeObject(largeObject);
        largeObjectService.deleteLargeObject(request.getImageObject().getId());

        return persistedObject;
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
