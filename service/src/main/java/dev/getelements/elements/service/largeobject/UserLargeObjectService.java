package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectBucket;
import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.largeobject.CreateLargeObjectFromUrlRequest;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;
import dev.getelements.elements.util.ValidationHelper;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

public class UserLargeObjectService implements LargeObjectService {

    private LargeObjectDao largeObjectDao;

    private LargeObjectBucket largeObjectBucket;

    private LargeObjectAccessUtils largeObjectAccessUtils;

    private ValidationHelper validationHelper;

    private Client client;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        Optional<LargeObject> result = getLargeObjectDao()
                .findLargeObject(objectId);
        if (result.isPresent() && !getLargeObjectAccessUtils().hasReadAccess(result.get())) {
            throw new ForbiddenException("User not allowed to find");
        }
        return result;
    }

    @Override
    public LargeObject updateLargeObject(final String objectId, final UpdateLargeObjectRequest objectRequest) {

        getValidationHelper().validateModel(objectRequest);
        final LargeObject largeObject = getLargeObject(objectId);

        if (!getLargeObjectAccessUtils().hasWriteAccess(largeObject)) {
            throw new ForbiddenException("User not allowed to update");
        }

        largeObject.setMimeType(objectRequest.getMimeType());

        return getLargeObjectAccessUtils().setCdnUrlToObject(largeObject);

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

        if (!getLargeObjectAccessUtils().hasDeleteAccess(largeObject)) {
            throw new ForbiddenException("User not allowed to delete");
        }

        getLargeObjectDao().deleteLargeObject(objectId);
        getLargeObjectBucket().deleteLargeObject(objectId);

    }

    @Override
    public InputStream readLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!getLargeObjectAccessUtils().hasReadAccess(largeObject)) {
            throw new ForbiddenException("User not allowed to read content");
        }

        return getLargeObjectBucket().readObject(largeObject.getId());
    }

    @Override
    public OutputStream writeLargeObjectContent(final String objectId) throws IOException {
        final var largeObject = getLargeObject(objectId);

        if (!getLargeObjectAccessUtils().hasWriteAccess(largeObject)) {
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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
