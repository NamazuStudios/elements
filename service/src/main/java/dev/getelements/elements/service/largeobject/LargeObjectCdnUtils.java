package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.LargeObject;

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
        final var url = format("%s/%s", getCdnUrl(), largeObject.getId());
        largeObject.setUrl(url);
        return largeObject;
    }

    public String getCdnUrl() {
        return cdnUrl;
    }

    @Inject
    public void setCdnUrl(@Named(CDN_OUTSIDE_URL) String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

}
