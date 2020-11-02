package com.namazustudios.socialengine.dao.mongo.application;

import com.mongodb.Mongo;
import com.mongodb.client.result.UpdateResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplicationConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoFirebaseApplicationConfiguration;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
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
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.namazustudios.socialengine.model.application.ConfigurationCategory.FIREBASE;

/**
 * This encapsulates the basic operations for handling the types derived from {@link MongoApplicationConfiguration}
 * reducing the boilerplate code needed to
 */
public class MongoApplicationConfigurationOperations {

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private Mapper beanMapper;

    private MongoDBUtils mongoDBUtils;

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    ApplicationConfigurationT createOrUpdateInactiveApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final Consumer<ApplicationConfigurationT> prevalidation,
            final Consumer<UpdateOperations<MongoApplicationConfigurationT>> processUpdateOperations,
            final String applicationNameOrId,
            final ApplicationConfigurationT applicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        prevalidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Create.class);

        final Query<MongoApplicationConfigurationT> query;
        query = getDatastore().find(mongoApplicationConfigurationClass);

        final String uniqueIdentifier = applicationConfiguration.getUniqueIdentifier();
        if (uniqueIdentifier == null) throw new IllegalArgumentException("uniqueIdentifier must be specified.");

        query.filter(Filters.and(
                Filters.eq("active", false),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", applicationConfiguration.getCategory()),
                Filters.eq("uniqueIdentifier", uniqueIdentifier)
        ));

        final UpdateResult updateResult = query.update(UpdateOperators.set("uniqueIdentifier", uniqueIdentifier),
                UpdateOperators.set("active", true),
                UpdateOperators.set("category", applicationConfiguration.getCategory()),
                UpdateOperators.set("parent", mongoApplication)
                ).execute(new UpdateOptions().upsert(true));

        final MongoApplicationConfigurationT mongoApplicationConfiguration;
        mongoApplicationConfiguration = getMongoDBUtils()
            .perform(ds -> {
                if(updateResult.getUpsertedId() != null){
                    final Query<MongoApplicationConfigurationT> qry = ds.find(mongoApplicationConfigurationClass);
                    return qry.filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
                }
                else{
                    return query.first();
                }
            });

        getObjectIndex().index(mongoApplicationConfiguration);
        return getBeanMapper().map(mongoApplicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    ApplicationConfigurationT getApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfigurationT> query;
        query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", category)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final MongoApplicationConfigurationT mongoApplicationConfiguration = query.first();

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found for " + applicationNameOrId);
        }

        return getBeanMapper().map(mongoApplicationConfiguration, applicationConfigurationClass);

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    List<ApplicationConfigurationT> getApplicationConfigurationsForApplication(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationNameOrId) {

        final MongoApplication parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final Query<MongoApplicationConfigurationT> query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(Filters.and(
                Filters.eq("parent", parent),
                Filters.eq("category", category)
        ));

        return query
            .iterator().toList().stream()
            .map(fac -> getBeanMapper().map(fac, applicationConfigurationClass))
            .collect(Collectors.toList());

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    ApplicationConfigurationT updateApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final Consumer<ApplicationConfigurationT> prevalidation,
            final Consumer<UpdateOperations<MongoApplicationConfigurationT>> processUpdateOperations,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final ApplicationConfigurationT applicationConfiguration) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        // Validate
        prevalidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Update.class);

        final Query<MongoApplicationConfigurationT> query;
        query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("parent", mongoApplication),
                Filters.eq("category", applicationConfiguration.getCategory())
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final String uniqueIdentifier = applicationConfiguration.getUniqueIdentifier();
        if (uniqueIdentifier == null) throw new IllegalArgumentException("uniqueIdentifier must be specified.");

        query.update(UpdateOperators.set("uniqueIdentifier", uniqueIdentifier),
                UpdateOperators.set("category", applicationConfiguration.getCategory()),
                UpdateOperators.set("parent", mongoApplication)
                ).execute(new UpdateOptions().upsert(false));

        final MongoApplicationConfigurationT mongoApplicationConfiguration;
        mongoApplicationConfiguration = getMongoDBUtils().perform(ds -> query.first());

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId + " not found for " + applicationNameOrId);
        }

        getObjectIndex().index(mongoApplicationConfiguration);
        return getBeanMapper().map(mongoApplicationConfiguration, applicationConfigurationClass);

    }

    public <MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    void softDeleteApplicationConfiguration(
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final ConfigurationCategory category,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfigurationT> query;
        query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(Filters.and(
           Filters.eq("active", true),
           Filters.eq("parent", mongoApplication),
           Filters.eq("category", category)
        ));

        try {
            query.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoApplicationConfigurationT mongoApplicationConfiguration;

        mongoApplicationConfiguration = getMongoDBUtils()
            .perform(ds -> {
                final Query<MongoApplicationConfigurationT> qry;
                qry = getDatastore().find(mongoApplicationConfigurationClass);

                try {
                    qry.filter(Filters.eq("_id", new ObjectId(applicationConfigurationNameOrId)));
                } catch (IllegalArgumentException ex) {
                    qry.filter(Filters.eq("uniqueIdentifier", applicationConfigurationNameOrId));
                }

                return qry.first();
            });

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
