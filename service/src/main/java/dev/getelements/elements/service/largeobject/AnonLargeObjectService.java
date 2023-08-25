package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.dao.LargeObjectDao;
import dev.getelements.elements.model.largeobject.CreateLargeObjectRequest;
import dev.getelements.elements.model.largeobject.LargeObject;
import dev.getelements.elements.model.largeobject.UpdateLargeObjectRequest;
import dev.getelements.elements.service.LargeObjectService;

import javax.inject.Inject;

public class AnonLargeObjectService implements LargeObjectService {

    private LargeObjectAccessUtils accessUtils;
    private LargeObjectServiceDriver driver;
    private LargeObjectDao largeObjectDao;

    @Override
    public LargeObject createLargeObject(CreateLargeObjectRequest objectRequest) {
        LargeObject newLargeObject = new LargeObject();
        String objectUrl = driver.createObject();

        //TODO: try to make it "transactional", so rollback storage, after crash below
        newLargeObject.setUrl(objectUrl);
        newLargeObject.setAccessPermissions(accessUtils.createAnonymousAccess());
        newLargeObject.setMimeType(newLargeObject.getMimeType());

        largeObjectDao.createOrUpdateLargeObject(newLargeObject);
        return newLargeObject;
    }

    @Override
    public LargeObject updateLargeObject(UpdateLargeObjectRequest objectRequest) {
        return null;
    }

    @Override
    public LargeObject getLargeObject(String objectId) {
        return null;
    }

    @Override
    public void deleteLargeObject(String objectId) {

    }

    public LargeObjectAccessUtils getAccessUtils() {
        return accessUtils;
    }

    @Inject
    public void setAccessUtils(LargeObjectAccessUtils accessUtils) {
        this.accessUtils = accessUtils;
    }

    public LargeObjectServiceDriver getDriver() {
        return driver;
    }

    @Inject
    public void setDriver(LargeObjectServiceDriver driver) {
        this.driver = driver;
    }

    public LargeObjectDao getLargeObjectDao() {
        return largeObjectDao;
    }

    @Inject
    public void setLargeObjectDao(LargeObjectDao largeObjectDao) {
        this.largeObjectDao = largeObjectDao;
    }
}
