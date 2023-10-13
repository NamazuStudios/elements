package dev.getelements.elements.service.profile;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.Subjects;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.service.LargeObjectService;

import javax.inject.Inject;

import java.util.UUID;

import static java.util.Arrays.asList;

public class ProfileImageObjectUtils {

    private static final String DEFAULT_IMAGE_PATH = "/user/%s/profile/%s/%s.bin";

    private LargeObjectService largeObjectService;
    
    public LargeObject createProfileImageObject(Profile profile) {
        LargeObject largeObject = new LargeObject();

        AccessPermissions imagePermissions = new AccessPermissions();
        imagePermissions.setRead(Subjects.wildcardSubject());
        imagePermissions.setWrite(getSubjectsWithProfileAccess(profile));
        imagePermissions.setDelete(getSubjectsWithProfileAccess(profile));
        largeObject.setAccessPermissions(imagePermissions);

        //TODO: should profile request provide this info?
        largeObject.setMimeType("image/png");
        largeObject.setPath(String.format(DEFAULT_IMAGE_PATH, profile.getUser().getId(), profile.getId(), UUID.randomUUID()));

        return largeObjectService.saveOrUpdateLargeObject(largeObject);
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
