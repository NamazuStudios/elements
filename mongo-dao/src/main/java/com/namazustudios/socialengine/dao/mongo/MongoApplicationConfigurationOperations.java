package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class MongoApplicationConfigurationOperations {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    public <ApplicationConfigurationT extends ApplicationConfiguration, UniqueIdentifierT>
    ApplicationConfigurationT createOrUpdateInactiveApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final String applicationNameOrId,
            final Consumer<ApplicationConfigurationT> additionalValidation,
            final Supplier<UniqueIdentifierT> extractUniqueIdentifier,
            final Consumer<UpdateOperations<ApplicationConfigurationT>> processUpdateOperations,
            final ApplicationConfigurationT applicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        // Validate
        additionalValidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Create.class);

        final Query<ApplicationConfigurationT> query;
        query = getDatastore().createQuery(applicationConfigurationClass);

        final UniqueIdentifierT uniqueIdentifier = extractUniqueIdentifier.get();

        query.and(
            query.criteria("active").equal(false),
            query.criteria("parent").equal(mongoApplication),
            query.criteria("category").equal(applicationConfiguration.getCategory()),
            query.criteria("uniqueIdentifier").equal(uniqueIdentifier)
        );

        final UpdateOperations<ApplicationConfigurationT> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(applicationConfigurationClass);

        updateOperations.set("uniqueIdentifier", uniqueIdentifier);
        updateOperations.set("active", true);
        updateOperations.set("category", applicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);
        processUpdateOperations.accept(updateOperations);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(true);

        final ApplicationConfigurationT mongoFacebookApplicationConfiguration;
        mongoFacebookApplicationConfiguration = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        getObjectIndex().index(mongoFacebookApplicationConfiguration);
        return getBeanMapper().map(mongoFacebookApplicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration>
    ApplicationConfigurationT getApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationConfigurationNameOrId) {

        final Query<ApplicationConfigurationT> query;
        query = getDatastore().createQuery(applicationConfigurationClass);

        query.and(
            query.criteria("active").equal(true),
            query.criteria( "category").equal(category)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final ApplicationConfigurationT applicationConfiguration = query.get();

        if (applicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found.");
        }

        return getBeanMapper().map(applicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration>
    ApplicationConfigurationT getApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<ApplicationConfigurationT> query;
        query = getDatastore().createQuery(applicationConfigurationClass);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("parent").equal(mongoApplication),
                query.criteria( "category").equal(category)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final ApplicationConfigurationT mongoApplicationConfiguration = query.get();

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found for " + applicationNameOrId);
        }

        return getBeanMapper().map(mongoApplicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration, UniqueIdentifierT>
    ApplicationConfigurationT updateApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Consumer<ApplicationConfigurationT> additionalValidation,
            final Supplier<UniqueIdentifierT> extractUniqueIdentifier,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final Consumer<UpdateOperations<ApplicationConfigurationT>> processUpdateOperations,
            final ApplicationConfigurationT applicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        // Validate
        additionalValidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Update.class);

        final Query<ApplicationConfigurationT> query;
        query = getDatastore().createQuery(applicationConfigurationClass);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").equal(applicationConfiguration.getCategory())
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<ApplicationConfigurationT> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(applicationConfigurationClass);

        final UniqueIdentifierT uniqueIdentifier = extractUniqueIdentifier.get();

        updateOperations.set("uniqueIdentifier", uniqueIdentifier);
        updateOperations.set("category", applicationConfiguration.getCategory());
        updateOperations.set("parent", mongoApplication);
        processUpdateOperations.accept(updateOperations);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final ApplicationConfigurationT mongoApplicationConfiguration;
        mongoApplicationConfiguration = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found for " + applicationNameOrId);
        }

        getObjectIndex().index(mongoApplicationConfiguration);
        return getBeanMapper().map(mongoApplicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration, UniqueIdentifierT>
    void softDeleteApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<ApplicationConfigurationT> query;
        query = getDatastore().createQuery(applicationConfigurationClass);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication),
            query.criteria( "category").equal(category)
        );

        try {
            query.filter("_id = ", new ObjectId(applicationConfigurationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("uniqueIdentifier = ", applicationConfigurationNameOrId);
        }

        final UpdateOperations<ApplicationConfigurationT> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(applicationConfigurationClass);
        updateOperations.set("active", false);

        final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .returnNew(true)
                .upsert(false);

        final ApplicationConfigurationT mongoApplicationConfiguration;

        mongoApplicationConfiguration = getMongoDBUtils()
            .perform(ds -> ds.findAndModify(query, updateOperations, findAndModifyOptions));

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found for " + applicationNameOrId);
        }

        getObjectIndex().index(mongoApplicationConfiguration);

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
