package com.namazustudios.socialengine.dao.mongo.application;

import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.FirebaseApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoFirebaseApplicationConfiguration;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.model.application.FirebaseApplicationConfiguration;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;

import java.util.List;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;

public class MongoFirebaseApplicationConfigurationDao extends MongoApplicationConfigurationDao
        implements FirebaseApplicationConfigurationDao {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public FirebaseApplicationConfiguration createOrUpdateInactiveApplicationConfiguration(
            final String applicationNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        validate(firebaseApplicationConfiguration);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", false),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", FIREBASE),
                Filters.eq("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim())
        ));

        final UpdateResult updateResult = query.update(UpdateOperators.set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim()),
        UpdateOperators.set("active", true),
        UpdateOperators.set("category", firebaseApplicationConfiguration.getCategory()),
        UpdateOperators.set("parent", mongoApplication),
        UpdateOperators.set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim())
        ).execute(new UpdateOptions().upsert(true));

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationProfile;
        mongoFirebaseApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    if(updateResult.getUpsertedId() != null) {
                        return ds.find(MongoFirebaseApplicationConfiguration.class)
                                .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                    } else {
                        return ds.find(MongoFirebaseApplicationConfiguration.class)
                                .filter(Filters.and(
                                        Filters.eq("active", true),
                                        Filters.eq("parent", mongoApplication),
                                        Filters.eq("category", FIREBASE),
                                        Filters.eq("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim())
                                )).first();
                    }
                });

        getObjectIndex().index(mongoFirebaseApplicationProfile);
        return getBeanMapper().map(mongoFirebaseApplicationProfile, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(final String applicationConfigurationNameOrId) {

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("category", FIREBASE)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration = query.first();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration getApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", FIREBASE)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration = query.first();

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("application profile " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public FirebaseApplicationConfiguration updateApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationProfileNameOrId,
            final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        validate(firebaseApplicationConfiguration);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", FIREBASE)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationProfileNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationProfileNameOrId));
        }

        query.update(UpdateOperators.set("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim()),
        UpdateOperators.set("category", firebaseApplicationConfiguration.getCategory()),
        UpdateOperators.set("parent", mongoApplication),
        UpdateOperators.set("serviceAccountCredentials", firebaseApplicationConfiguration.getServiceAccountCredentials().trim())
        ).execute(new UpdateOptions().upsert(false));

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationConfiguration;
        mongoFirebaseApplicationConfiguration = getMongoDBUtils()
                .perform(ds -> ds.find(MongoFirebaseApplicationConfiguration.class)
                        .filter(Filters.and(
                                Filters.eq("active", true),
                                Filters.eq("parent", mongoApplication),
                                Filters.eq( "category", FIREBASE),
                                Filters.eq("uniqueIdentifier", firebaseApplicationConfiguration.getProjectId().trim())
                        )).first()
                );

        if (mongoFirebaseApplicationConfiguration == null) {
            throw new NotFoundException("profile with ID not found: " + applicationProfileNameOrId);
        }

        getObjectIndex().index(mongoFirebaseApplicationConfiguration);
        return getBeanMapper().map(mongoFirebaseApplicationConfiguration, FirebaseApplicationConfiguration.class);

    }

    @Override
    public void softDeleteApplicationConfiguration(
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoFirebaseApplicationConfiguration> query;
        query = getDatastore().find(MongoFirebaseApplicationConfiguration.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq( "category", FIREBASE)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoFirebaseApplicationConfiguration mongoFirebaseApplicationProfile;

        mongoFirebaseApplicationProfile = getMongoDBUtils()
                .perform(ds -> {
                    final Query<MongoFirebaseApplicationConfiguration> qry = getDatastore().find(MongoFirebaseApplicationConfiguration.class);
                    qry.filter(Filters.and(
                            Filters.eq("active", false),
                            Filters.eq("parent", mongoApplication),
                            Filters.eq( "category", FIREBASE)
                    ));

                    try {
                        qry.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
                    } catch (IllegalArgumentException ex) {
                        qry.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
                    }
                    return qry.first();
                });

        if (mongoFirebaseApplicationProfile == null) {
            throw new NotFoundException("profile with ID not found: " + mongoFirebaseApplicationProfile.getObjectId());
        }

        getObjectIndex().index(mongoFirebaseApplicationProfile);

    }

    public void validate(final FirebaseApplicationConfiguration firebaseApplicationConfiguration) {

        if (firebaseApplicationConfiguration == null) {
            throw new InvalidDataException("firebaseApplicationConfiguration must not be null.");
        }

        if (firebaseApplicationConfiguration.getCategory() == null) {
            firebaseApplicationConfiguration.setCategory(ConfigurationCategory.FIREBASE);
        }

        switch (firebaseApplicationConfiguration.getCategory()) {
            case FIREBASE:
                break;
            default:
                throw new InvalidDataException("platform not supported: " + firebaseApplicationConfiguration.getCategory());
        }

        getValidationHelper().validateModel(firebaseApplicationConfiguration);

    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
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
