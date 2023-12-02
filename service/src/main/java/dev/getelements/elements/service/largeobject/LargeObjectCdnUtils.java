package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.LargeObjectReference;
import dev.getelements.elements.model.profile.Profile;

import javax.inject.Inject;
import javax.inject.Named;

import static dev.getelements.elements.Constants.CDN_OUTSIDE_URL;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class LargeObjectCdnUtils {

    private String cdnUrl;

    public String assignAutomaticPath(final String mimeType) {
        return format("/_auto/%s/%s.bin", mimeType, randomUUID());
    }

    public LargeObject setCdnUrlToObject(final LargeObject largeObject) {
        largeObject.setUrl(assembleCdnUrl(largeObject.getId()));
        return largeObject;
    }

    public String assembleCdnUrl(String id) {
        return format("%s/object/%s", getCdnUrl(), id);
    }

    public Profile setProfileCdnUrl(Profile profile) {
        LargeObjectReference reference = profile.getImageObject();
        reference.setUrl(assembleCdnUrl(reference.getId()));
        return profile;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    @Inject
    public void setCdnUrl(@Named(CDN_OUTSIDE_URL) String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }
}
