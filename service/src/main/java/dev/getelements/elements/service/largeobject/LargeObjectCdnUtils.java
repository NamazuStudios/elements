package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.profile.Profile;

import jakarta.inject.Inject;
import jakarta.inject.Named;

import static dev.getelements.elements.sdk.model.Constants.CDN_OUTSIDE_URL;
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
        if (profile != null && profile.getImageObject() != null) {
            LargeObjectReference reference = profile.getImageObject();
            reference.setUrl(assembleCdnUrl(reference.getId()));
        }
        return profile;
    }

    public DistinctInventoryItem setDistinctItemProfileCdnUrl(DistinctInventoryItem item) {
        if (item != null) {
            setProfileCdnUrl(item.getProfile());
        }
        return item;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    @Inject
    public void setCdnUrl(@Named(CDN_OUTSIDE_URL) String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }
}
