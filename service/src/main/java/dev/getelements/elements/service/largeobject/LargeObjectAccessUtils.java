package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.model.largeobject.AccessPermissions;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.SubjectRequest;

import javax.inject.Inject;
import javax.inject.Named;

import static dev.getelements.elements.Constants.CDN_OUTSIDE_URL;
import static java.lang.String.format;
import static java.util.UUID.randomUUID;

public class LargeObjectAccessUtils {

    private String cdnUrl;

    private AccessPermissionsUtils accessPermissionsUtils;

    public String assignAutomaticPath(final String mimeType) {
        return format("/_auto/%s/%s.bin", mimeType, randomUUID());
    }

    public boolean hasReadAccess(final LargeObject largeObject) {
        return getAccessPermissionsUtils().hasReadAccess(largeObject.getAccessPermissions());
    }

    public boolean hasWriteAccess(final LargeObject largeObject) {
        return getAccessPermissionsUtils().hasWriteAccess(largeObject.getAccessPermissions());
    }

    public boolean hasDeleteAccess(final LargeObject largeObject) {
        return getAccessPermissionsUtils().hasDeleteAccess(largeObject.getAccessPermissions());
    }

    public AccessPermissions createAccessPermissions(
            final SubjectRequest readRequest,
            final SubjectRequest writeRequest,
            final SubjectRequest deleteRequest) {
        final var accessPermissions = new AccessPermissions();
        accessPermissions.setRead(getAccessPermissionsUtils().fromRequest(readRequest));
        accessPermissions.setWrite(getAccessPermissionsUtils().fromRequest(writeRequest));
        accessPermissions.setDelete(getAccessPermissionsUtils().fromRequest(deleteRequest));
        return accessPermissions;
    }

    public LargeObject setCdnUrlToObject(final LargeObject largeObject) {
        final var url = format("%s/%s", cdnUrl, largeObject.getId());
        largeObject.setUrl(url);
        return largeObject;
    }

    @Inject
    public void setCdnUrl(@Named(CDN_OUTSIDE_URL) String cdnUrl) {
        this.cdnUrl = cdnUrl;
    }

    public AccessPermissionsUtils getAccessPermissionsUtils() {
        return accessPermissionsUtils;
    }

    @Inject
    public void setAccessPermissionsUtils(AccessPermissionsUtils accessPermissionsUtils) {
        this.accessPermissionsUtils = accessPermissionsUtils;
    }

}
