package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.PSNApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.model.application.Platform.PSN_PS4;
import static com.namazustudios.socialengine.model.application.Platform.PSN_VITA;
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

    private MongoDBUtils mongoDBUtils;

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
            query.criteria("platform").in(newArrayList(PSN_PS4, PSN_VITA)),
            query.criteria("name").equal(psnApplicationProfile.getNpIdentifier())
        );

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("name", psnApplicationProfile.getNpIdentifier().trim());
        updateOperations.set("client_secret", nullToEmpty(psnApplicationProfile.getClientSecret()).trim());
        updateOperations.set("active", false);
        updateOperations.set("platform", psnApplicationProfile.getPlatform());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationProfile.class);

    }

    @Override
    public PSNApplicationProfile getPSNApplicationProfile(final String applicationNameOrId,
                                                          final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationProfile> query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").in(newArrayList(PSN_VITA, PSN_PS4))
        );

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

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(psnApplicationProfile);

        final Query<MongoPSNApplicationProfile> query;
        query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").in(newArrayList(PSN_VITA, PSN_PS4))
        );

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

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        getObjectIndex().index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationProfile.class);

    }

    @Override
    public void softDeleteApplicationProfile(final String applicationNameOrId,
                                             final String applicationProfileNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoPSNApplicationProfile> query;
        query = getDatastore().createQuery(MongoPSNApplicationProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("platform").in(newArrayList(PSN_VITA, PSN_PS4))
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoPSNApplicationProfile> updateOperations =
                getDatastore().createUpdateOperations(MongoPSNApplicationProfile.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoPSNApplicationProfile mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        getObjectIndex().index(mongoPSNApplicationProfile);

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

        getValidationHelper().validateModel(psnApplicationProfile);

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

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
