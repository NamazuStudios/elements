package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.*;
import dev.getelements.elements.model.user.User;
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

    private LargeObjectAccessUtils largeObjectAccessUtils;

    private ValidationHelper validationHelper;

    private User user;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        final var largeObject = getLargeObjectDao().findLargeObject(objectId);

        if (largeObject.isPresent() && !largeObjectAccessUtils.hasReadAccess(largeObject.get().getAccessPermissions(), user)) {
            throw new ForbiddenException();
        }

        return largeObject.map(getLargeObjectAccessUtils()::setCdnUrlToObject);
    }

    @Override
    public LargeObject updateLargeObject(final String objectId, final UpdateLargeObjectRequest objectRequest) {

        getValidationHelper().validateModel(objectRequest);
        final var largeObject = getLargeObject(objectId);

        if (!largeObjectAccessUtils.hasWriteAccess(largeObject.getAccessPermissions(), user)) {
            throw new ForbiddenException();
        }

        return getLargeObjectAccessUtils().setCdnUrlToObject(largeObject);
    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void deleteLargeObject(final String objectId) throws IOException {

        final var largeObject = getLargeObject(objectId);

        // TODO: Add delete field to access permissions and update all DTOs
        if (!largeObjectAccessUtils.hasWriteAccess(largeObject.getAccessPermissions(), user)) {
            throw new ForbiddenException();
        }

        getLargeObjectDao().deleteLargeObject(objectId);
        getLargeObjectBucket().deleteLargeObject(objectId);

    }

    @Override
    public InputStream readLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!largeObjectAccessUtils.hasReadAccess(largeObject.getAccessPermissions(), user)) {
            throw new ForbiddenException();
        }

        return getLargeObjectBucket().readObject(largeObject.getId());
    }

    @Override
    public OutputStream writeLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!largeObjectAccessUtils.hasWriteAccess(largeObject.getAccessPermissions(), user)) {
            throw new ForbiddenException();
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

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public LargeObjectAccessUtils getLargeObjectAccessUtils() {
        return largeObjectAccessUtils;
    }

    @Inject
    public void setLargeObjectAccessUtils(LargeObjectAccessUtils largeObjectAccessUtils) {
        this.largeObjectAccessUtils = largeObjectAccessUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }
}
