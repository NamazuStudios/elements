package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.PSNApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoPSNApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.PSN_PS4;
import static com.namazustudios.socialengine.model.application.ConfigurationCategory.PSN_VITA;
import static java.util.Arrays.asList;


/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoPSNApplicationConfigurationDao implements PSNApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private MongoApplicationDao mongoApplicationDao;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public PSNApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(final String applicationNameOrId,
                                                                                      final PSNApplicationConfiguration psnApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(psnApplicationConfiguration);

        final Query<MongoPSNApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoPSNApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").in(asList(PSN_PS4, PSN_VITA)),
            query.criteria("uniqueIdentifier").equal(psnApplicationConfiguration.getNpIdentifier())
        );

        final UpdateOperations<MongoPSNApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim());
        updateOperations.set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim());
        updateOperations.set("active", false);
        updateOperations.set( "category", psnApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(true);

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public PSNApplicationConfiguration getPSNApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationConfiguration> query = getDatastore().createQuery(MongoPSNApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").in(asList(PSN_VITA, PSN_PS4))
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile = query.get();

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public PSNApplicationConfiguration updateApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationProfileNameOrId,
                                                                      final PSNApplicationConfiguration psnApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(psnApplicationConfiguration);

        final Query<MongoPSNApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoPSNApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").in(asList(PSN_VITA, PSN_PS4))
        );

        try {
            query.filter("_id = ", new ObjectId(applicationProfileNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationProfileNameOrId);
        }

        final UpdateOperations<MongoPSNApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationConfiguration.class);

        updateOperations.set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim());
        updateOperations.set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim());
        updateOperations.set( "category", psnApplicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        getObjectIndex().index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(final String applicationNameOrId,
                                                   final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoPSNApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoPSNApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").in(asList(PSN_VITA, PSN_PS4))
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<MongoPSNApplicationConfiguration> updateOperations =
                getDatastore().createUpdateOperations(MongoPSNApplicationConfiguration.class);

        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoPSNApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoPSNApplicationProfile);
        }

        getObjectIndex().index(mongoPSNApplicationProfile);

    }

    public void validate(final PSNApplicationConfiguration psnApplicationProfile) {

        if (psnApplicationProfile == null) {
            throw new InvalidDataException("psnApplicationProfile must not be null.");
        }

        switch (psnApplicationProfile.getCategory()) {
            case PSN_PS4:
            case PSN_VITA:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + psnApplicationProfile.getCategory());
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
