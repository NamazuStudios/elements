package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.largeobject.*;
import dev.getelements.elements.sdk.model.util.ValidationHelper;

import dev.getelements.elements.sdk.service.largeobject.LargeObjectService;
import jakarta.inject.Inject;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.WebTarget;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Optional;

import static java.util.Objects.isNull;

public class SuperUserLargeObjectService implements LargeObjectService {

    private ValidationHelper validationHelper;

    private LargeObjectDao largeObjectDao;

    private LargeObjectBucket largeObjectBucket;

    private AccessRequestUtils accessRequestUtils;

    private LargeObjectCdnUtils largeObjectCdnUtils;

    private Client client;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return getLargeObjectDao().findLargeObject(objectId).map(getLargeObjectCdnUtils()::setCdnUrlToObject);
    }

    @Override
    public Pagination<LargeObject> getLargeObjects(final int offset, final int count, final String search) {
        return getLargeObjectDao().getLargeObjects(offset, count, search);
    }

    @Override
    public LargeObject updateLargeObject(
            final String objectId,
            final UpdateLargeObjectRequest updateLargeObjectRequest) {

        getValidationHelper().validateModel(updateLargeObjectRequest);

        final var largeObject = getLargeObject(objectId);

        AccessPermissions accessPermissions = getAccessRequestUtils().createAccessPermissions(
                updateLargeObjectRequest.getRead(),
                updateLargeObjectRequest.getWrite(),
                updateLargeObjectRequest.getDelete()
        );

        largeObject.setMimeType(updateLargeObjectRequest.getMimeType());
        largeObject.setAccessPermissions(accessPermissions);

        return getLargeObjectCdnUtils().setCdnUrlToObject(getLargeObjectDao().updateLargeObject(largeObject));
    }

    @Override
    public LargeObject createLargeObject(final CreateLargeObjectRequest createLargeObjectRequest) {

        getValidationHelper().validateModel(createLargeObjectRequest);
        final var largeObject = new LargeObject();
        AccessPermissions accessPermissions = getAccessRequestUtils().createAccessPermissions(
                createLargeObjectRequest.getRead(),
                createLargeObjectRequest.getWrite(),
                createLargeObjectRequest.getDelete()
        );

        largeObject.setMimeType(createLargeObjectRequest.getMimeType());
        largeObject.setAccessPermissions(accessPermissions);
        largeObject.setPath(getLargeObjectCdnUtils().assignAutomaticPath(createLargeObjectRequest.getMimeType()));

        return getLargeObjectCdnUtils().setCdnUrlToObject(getLargeObjectDao().createLargeObject(largeObject));
    }

    @Override
    public LargeObject createLargeObjectFromUrl(final CreateLargeObjectFromUrlRequest createRequest) throws IOException {

        getValidationHelper().validateModel(createRequest);
        WebTarget target = client.target(createRequest.getFileUrl());
        InputStream is = target.request().get().readEntity(InputStream.class);
        return createLargeObject(createRequest, is);
    }

    @Override
    public void deleteLargeObject(final String objectId) {

        try {
            getLargeObjectBucket().deleteLargeObject(objectId);
        } catch (IOException e) {
            throw new InternalException("Caught IO Exception processing request.");
        }

        largeObjectDao.deleteLargeObject(objectId);

    }

    @Override
    public InputStream readLargeObjectContent(final String objectId) throws IOException {
        return getLargeObjectBucket().readObject(objectId);
    }

    @Override
    public OutputStream writeLargeObjectContent(final String objectId) throws IOException {
        return getLargeObjectBucket().writeObject(objectId);
    }

    @Override
    public LargeObject saveOrUpdateLargeObject(LargeObject largeObject) {
        getValidationHelper().validateModel(largeObject);
        return isNull(largeObject.getId()) ?
                getLargeObjectDao().createLargeObject(largeObject) : getLargeObjectDao().updateLargeObject(largeObject);
    }

    public LargeObjectCdnUtils getLargeObjectCdnUtils() {
        return largeObjectCdnUtils;
    }

    @Inject
    public void setLargeObjectCdnUtils(LargeObjectCdnUtils largeObjectCdnUtils) {
        this.largeObjectCdnUtils = largeObjectCdnUtils;
    }

    public AccessRequestUtils getAccessRequestUtils() {
        return accessRequestUtils;
    }

    @Inject
    public void setAccessRequestUtils(AccessRequestUtils accessRequestUtils) {
        this.accessRequestUtils = accessRequestUtils;
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

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }
}
