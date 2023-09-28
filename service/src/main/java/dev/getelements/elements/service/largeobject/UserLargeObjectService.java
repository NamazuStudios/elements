package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;
import dev.getelements.elements.util.ValidationHelper;

import javax.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class UserLargeObjectService implements LargeObjectService {

    private LargeObjectDao largeObjectDao;

    private LargeObjectBucket largeObjectBucket;

    private AccessPermissionsUtils accessPermissionsUtils;

    private ValidationHelper validationHelper;

    private LargeObjectCdnUtils largeObjectCdnUtils;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        final Optional<LargeObject> result = getLargeObjectDao().findLargeObject(objectId);

        if (result.isPresent() && !getAccessPermissionsUtils().hasReadAccess(result.get().getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to find");
        }

        return result;
    }

    @Override
    public LargeObject updateLargeObject(final String objectId, final UpdateLargeObjectRequest objectRequest) {

        getValidationHelper().validateModel(objectRequest);
        final LargeObject largeObject = getLargeObject(objectId);

        if (!getAccessPermissionsUtils().hasWriteAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to update");
        }

        largeObject.setMimeType(objectRequest.getMimeType());

        // TODO: verify if update accessPermissions makes any sense
        // TODO: No. Not at this point in time. We can address this later if requirements dictate.

        return getLargeObjectCdnUtils().setCdnUrlToObject(largeObject);

    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        throw new ForbiddenException("User not allowed to create");
    }

    @Override
    public void deleteLargeObject(final String objectId) throws IOException {

        final var largeObject = getLargeObject(objectId);

        if (!getAccessPermissionsUtils().hasDeleteAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to delete");
        }

        getLargeObjectDao().deleteLargeObject(objectId);
        getLargeObjectBucket().deleteLargeObject(objectId);

    }

    @Override
    public InputStream readLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!getAccessPermissionsUtils().hasReadAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to read content");
        }

        return getLargeObjectBucket().readObject(largeObject.getId());
    }

    @Override
    public OutputStream writeLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!getAccessPermissionsUtils().hasWriteAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to write content");
        }

        return getLargeObjectBucket().writeObject(objectId);
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }

    public LargeObjectBucket getLargeObjectBucket() {
        return largeObjectBucket;
    }

    @Inject
    public void setLargeObjectBucket(LargeObjectBucket largeObjectBucket) {
        this.largeObjectBucket = largeObjectBucket;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public AccessPermissionsUtils getAccessPermissionsUtils() {
        return accessPermissionsUtils;
    }

    @Inject
    public void setAccessPermissionsUtils(AccessPermissionsUtils accessPermissionsUtils) {
        this.accessPermissionsUtils = accessPermissionsUtils;
    }

    public LargeObjectCdnUtils getLargeObjectCdnUtils() {
        return largeObjectCdnUtils;
    }

    @Inject
    public void setLargeObjectCdnUtils(LargeObjectCdnUtils largeObjectCdnUtils) {
        this.largeObjectCdnUtils = largeObjectCdnUtils;
    }

}
