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

    private AccessRequestUtils accessRequestUtils;

    private LargeObjectCdnUtils largeObjectCdnUtils;

    @Override
    public Optional<LargeObject> findLargeObject(final String objectId) {
        return getLargeObjectDao().findLargeObject(objectId).map(getLargeObjectCdnUtils()::setCdnUrlToObject);
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


}
