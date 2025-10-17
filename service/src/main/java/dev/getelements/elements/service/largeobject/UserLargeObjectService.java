package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.sdk.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import jakarta.inject.Inject;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.util.Objects.isNull;

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

        return result.map(getLargeObjectCdnUtils()::setCdnUrlToObject);
    }

    @Override
    public Pagination<LargeObject> getLargeObjects(final int offset, final int count, final String search) {
        return getLargeObjectDao().getLargeObjects(offset, count, search);
    }

    @Override
    public LargeObject updateLargeObject(final String objectId, final UpdateLargeObjectRequest objectRequest) {

        getValidationHelper().validateModel(objectRequest);
        final LargeObject largeObject = getLargeObject(objectId);

        if (!getAccessPermissionsUtils().hasWriteAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to update");
        }

        largeObject.setMimeType(objectRequest.getMimeType());

        return getLargeObjectCdnUtils().setCdnUrlToObject(largeObject);

    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        throw new ForbiddenException("User not allowed to create");
    }

    @Override
    public LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) {
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

    @Override
    public LargeObject saveOrUpdateLargeObject(LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject);

        if (!getAccessPermissionsUtils().hasWriteAccess(largeObject.getAccessPermissions())) {
            throw new ForbiddenException("User not allowed to save or update large object");
        }

        return getLargeObjectCdnUtils().setCdnUrlToObject( isNull(largeObject.getId()) ?
                getLargeObjectDao().createLargeObject(largeObject) : getLargeObjectDao().updateLargeObject(largeObject));
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
