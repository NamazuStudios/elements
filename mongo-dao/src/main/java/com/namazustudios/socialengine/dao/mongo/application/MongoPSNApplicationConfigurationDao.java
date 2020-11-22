package com.namazustudios.socialengine.dao.mongo.application;

import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.PSNApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoPSNApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
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

    private Datastore datastore;

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
        query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", false),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", asList(PSN_PS4, PSN_VITA)),
                Filters.eq("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier())
        ));

        final UpdateResult updateResult = query.update(UpdateOperators.set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim()),
                UpdateOperators.set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim()),
                UpdateOperators.set("active", false),
                UpdateOperators.set( "category", psnApplicationConfiguration.getCategory()),
                UpdateOperators.set("parent", mongoApplication)
        ).execute(new UpdateOptions().upsert(true));

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> {
                if(updateResult.getUpsertedId() != null){
                    return ds.find(MongoPSNApplicationConfiguration.class)
                            .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                } else {
                    return ds.find(MongoPSNApplicationConfiguration.class)
                            .filter(Filters.and(
                                    Filters.eq("active", false),
                                    Filters.eq("parent", mongoApplication),
                                    Filters.eq( "category", asList(PSN_PS4, PSN_VITA)),
                                    Filters.eq("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier())
                            )).first();
                }
            });

        getObjectIndex().index(mongoPSNApplicationProfile);
        return getBeanMapper().map(mongoPSNApplicationProfile, PSNApplicationConfiguration.class);

    }

    @Override
    public PSNApplicationConfiguration getPSNApplicationConfiguration(final String applicationNameOrId,
                                                                      final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final Query<MongoPSNApplicationConfiguration> query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.in( "category", asList(PSN_VITA, PSN_PS4))
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile = query.first();

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
        query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.in( "category", asList(PSN_VITA, PSN_PS4))
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationProfileNameOrId));
        }

        final UpdateOperations<MongoPSNApplicationConfiguration> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoPSNApplicationConfiguration.class);

        query.update(UpdateOperators.set("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim()),
                UpdateOperators.set("client_secret", nullToEmpty(psnApplicationConfiguration.getClientSecret()).trim()),
                UpdateOperators.set( "category", psnApplicationConfiguration.getCategory()),
                UpdateOperators.set("parent", mongoApplication)
        ).execute(new UpdateOptions().upsert(false));

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> ds.find(MongoPSNApplicationConfiguration.class)
                    .filter(Filters.and(
                            Filters.eq("active", true),
                            Filters.eq("parent", mongoApplication),
                            Filters.in( "category", asList(PSN_VITA, PSN_PS4)),
                            Filters.eq("uniqueIdentifier", psnApplicationConfiguration.getNpIdentifier().trim())
                    )).first()
            );

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
        query = getDatastore().find(MongoPSNApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.in( "category", asList(PSN_VITA, PSN_PS4))
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoPSNApplicationConfiguration mongoPSNApplicationProfile;

        mongoPSNApplicationProfile = getMongoDBUtils()
            .perform(ds -> {
                try {
                    return ds.find(MongoPSNApplicationConfiguration.class)
                            .filter(Filters.and(
                                    Filters.eq("active", true),
                                    Filters.eq("parent", mongoApplication),
                                    Filters.in( "category", asList(PSN_VITA, PSN_PS4)),
                                    Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId))
                            )).first();
                } catch (IllegalArgumentException ex) {
                    return ds.find(MongoPSNApplicationConfiguration.class)
                            .filter(Filters.and(
                                    Filters.eq("active", true),
                                    Filters.eq("parent", mongoApplication),
                                    Filters.in( "category", asList(PSN_VITA, PSN_PS4)),
                                    Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId)
                            )).first();
                }
            });

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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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
