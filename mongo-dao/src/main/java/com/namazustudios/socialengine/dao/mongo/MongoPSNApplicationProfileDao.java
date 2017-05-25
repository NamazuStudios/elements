package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.PSNApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import com.namazustudios.socialengine.model.application.Platform;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static org.testng.collections.Lists.newArrayList;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoPSNApplicationProfileDao implements PSNApplicationProfileDao {

    private ObjectIndex objectIndex;

    private MongoApplicationDao mongoApplicationDao;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    @Override
    public PSNApplicationProfile createOrUpdateInactiveApplicationProfile(final String applicationNameOrId,
                                                                          final PSNApplicationProfile psnApplicationProfile) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(psnApplicationProfile);

        final Query<MongoPSNApplicationProfile> query;
        query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.and(
                query.criteria("active").equal(false),
                query.criteria("parent").equal(mongoApplication),
                query.criteria("platform").in(newArrayList(
                        Platform.PSN_PS4,
                        Platform.PSN_VITA
                )),
                query.criteria("name").equal(psnApplicationProfile.getNpIdentifier())
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
                datastore.createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("name", psnApplicationProfile.getNpIdentifier().trim());
        updateOperations.set("client_secret", nullToEmpty(psnApplicationProfile.getClientSecret()).trim());
        updateOperations.set("active", true);
        updateOperations.set("platform", psnApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = getDatastore().findAndModify(query, updateOperations, false, true);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        objectIndex.index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationProfile.class);

    }

    @Override
    public PSNApplicationProfile getPSNApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication = mongoApplicationDao.getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationProfile> query = datastore.createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform in", new Object[]{Platform.PSN_VITA, Platform.PSN_PS4});

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final MongoPSNApplicationProfile mongoPSNApplicationProfile = query.get();

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationProfileNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationProfile.class);

    }

    @Override
    public PSNApplicationProfile updateApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId,
                                                          final PSNApplicationProfile psnApplicationProfile) {

        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(psnApplicationProfile);

        final Query<MongoPSNApplicationProfile> query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform in", new Object[]{Platform.PSN_VITA, Platform.PSN_PS4});

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("name", psnApplicationProfile.getNpIdentifier().trim());
        updateOperations.set("client_secret", nullToEmpty(psnApplicationProfile.getClientSecret()).trim());
        updateOperations.set("platform", psnApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = getDatastore().findAndModify(query, updateOperations, false, false);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        objectIndex.index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationProfile.class);

    }

    @Override
    public void softDeleteApplicationProfile(final String applicationNameOrId,
                                             final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoPSNApplicationProfile> query;
        query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);
        query.filter("platform in", new Object[]{Platform.PSN_VITA, Platform.PSN_PS4});

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
                datastore.createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("active", false);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        try {
            mongoPSNApplicationProfile = datastore.findAndModify(query, updateOperations, false, false);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        objectIndex.index(mongoPSNApplicationProfile);

    }

    public void validate(final PSNApplicationProfile psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getPlatform()) {
            case PSN_PS4:
            case PSN_VITA:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getPlatform());
        }

        validationHelper.validateModel(psnApplicationProfile);

    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }
    
}
