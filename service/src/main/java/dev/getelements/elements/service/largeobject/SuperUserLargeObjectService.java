package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.largeobject.AccessPermissions;
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

public class SuperUserLargeObjectService implements LargeObjectService {

    private ValidationHelper validationHelper;

    private LargeObjectDao largeObjectDao;

    private LargeObjectBucket largeObjectBucket;

    private LargeObjectAccessUtils largeObjectAccessUtils;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return getLargeObjectDao().findLargeObject(objectId);
    }

    @Override
    public LargeObject updateLargeObject(
            final String objectId,
            final UpdateLargeObjectRequest updateLargeObjectRequest) {

        getValidationHelper().validateModel(updateLargeObjectRequest);

        final var largeObject = getLargeObjectDao().getLargeObject(objectId);

        final var read = getLargeObjectAccessUtils().fromRequest(updateLargeObjectRequest.getRead());
        final var write = getLargeObjectAccessUtils().fromRequest(updateLargeObjectRequest.getWrite());
        final var accessPermissions = new AccessPermissions();
        accessPermissions.setRead(read);
        accessPermissions.setWrite(write);

        largeObject.setMimeType(updateLargeObjectRequest.getMimeType());
        largeObject.setAccessPermissions(accessPermissions);

        return getLargeObjectDao().updateLargeObject(largeObject);

    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {

        getValidationHelper().validateModel(createLargeObjectRequest);

        final var largeObject = new LargeObject();

        final var read = getLargeObjectAccessUtils().fromRequest(createLargeObjectRequest.getRead());
        final var write = getLargeObjectAccessUtils().fromRequest(createLargeObjectRequest.getWrite());
        final var accessPermissions = new AccessPermissions();
        accessPermissions.setRead(read);
        accessPermissions.setWrite(write);

        largeObject.setMimeType(createLargeObjectRequest.getMimeType());
        largeObject.setAccessPermissions(accessPermissions);
        largeObject.setPath(getLargeObjectAccessUtils().assignAutomaticPath(createLargeObjectRequest.getMimeType()));

        return getLargeObjectDao().updateLargeObject(largeObject);

    }

    @Override
    public void deleteLargeObject(String objectId) {
        try {
            getLargeObjectBucket().deleteLargeObject(objectId);
        } catch (IOException e) {
            throw new InternalException("Caught IO Exception processing request.");
        }
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    @Override
    public InputStream readLargeObject(final String objectId) throws IOException {
        return getLargeObjectBucket().readObject(objectId);
    }

    @Override
    public OutputStream writeLargeObject(final String objectId) throws IOException {
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

    public LargeObjectAccessUtils getLargeObjectAccessUtils() {
        return largeObjectAccessUtils;
    }

    @Inject
    public void setLargeObjectAccessUtils(LargeObjectAccessUtils largeObjectAccessUtils) {
        this.largeObjectAccessUtils = largeObjectAccessUtils;
    }

//    @Override
//    public LargeObject createLargeObject(CreateLargeObjectRequest createLargeObjectRequest, InputStream inputStream) {
//        LargeObject newLargeObject = new LargeObject();
//        String objectUrl = driver.createObject(inputStream, createLargeObjectRequest);
//
//        //TODO: try to make it "transactional", so rollback storage, after crash below
//        newLargeObject.setUrl(objectUrl);
//        newLargeObject.setAccessPermissions(accessUtils.createAnonymousAccess());
//        newLargeObject.setMimeType(newLargeObject.getMimeType());
//
//        largeObjectDao.createLargeObject(newLargeObject);
//        return newLargeObject;
//    }

}
