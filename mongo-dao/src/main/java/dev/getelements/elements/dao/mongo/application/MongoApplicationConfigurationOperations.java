package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.application.MongoApplicationConfiguration;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.ApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.ConfigurationCategory;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.Query;
import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.util.List;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.set;

/**
 * This encapsulates the basic operations for handling the types derived from {@link MongoApplicationConfiguration}
 * reducing the boilerplate code needed to
 */
public class MongoApplicationConfigurationOperations {

    private Datastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    private ValidationHelper validationHelper;

    private MapperRegistry beanMapperRegistry;

    private MongoDBUtils mongoDBUtils;

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    ApplicationConfigurationT createOrUpdateInactiveApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final Consumer<ApplicationConfigurationT> preValidation,
            final Consumer<UpdateBuilder> processModifyBuilder,
            final String applicationNameOrId,
            final ApplicationConfigurationT applicationConfiguration) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        preValidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Create.class);

        final var query = getDatastore().find(mongoApplicationConfigurationClass);

        final var uniqueIdentifier = applicationConfiguration.getName();
        if (uniqueIdentifier == null) throw new IllegalArgumentException("uniqueIdentifier must be specified.");

        query.filter(
            and(
                eq("active", false),
                eq("parent", mongoApplication),
                eq("category", applicationConfiguration.getCategory()),
                eq("uniqueIdentifier", uniqueIdentifier)
            )
        );

        final var builder = new UpdateBuilder();
        processModifyBuilder.accept(builder);

        final var mongoApplicationConfiguration = getMongoDBUtils().perform(ds ->
            builder.with(
                set("uniqueIdentifier", uniqueIdentifier),
                set("active", true),
                set("category", applicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

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

        final var query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", category)
            )
        );

        try {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoApplicationConfiguration = query.first();

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

        final var parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);
        final var query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(
            and(
                eq("parent", parent),
                eq("category", category)
            )
        );

        try (var iterator = query.iterator()) {
            return iterator
                    .toList()
                    .stream()
                    .map(fac -> getBeanMapper().map(fac, applicationConfigurationClass))
                    .collect(Collectors.toList());
        }

    }

    public <ApplicationConfigurationT extends ApplicationConfiguration,
            MongoApplicationConfigurationT extends MongoApplicationConfiguration>
    ApplicationConfigurationT updateApplicationConfiguration(
            final Class<ApplicationConfigurationT> applicationConfigurationClass,
            final Class<MongoApplicationConfigurationT> mongoApplicationConfigurationClass,
            final Consumer<ApplicationConfigurationT> prevalidation,
            final Consumer<UpdateBuilder> processModifyBuilder,
            final String applicationNameOrId,
            final String applicationConfigurationNameOrId,
            final ApplicationConfigurationT applicationConfiguration) {

        final var mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        // Validate
        prevalidation.accept(applicationConfiguration);
        getValidationHelper().validateModel(applicationConfiguration, ValidationGroups.Update.class);

        final var query = getDatastore().find(mongoApplicationConfigurationClass);

        query.filter(
            and(
                eq("active", true),
                eq("parent", mongoApplication),
                eq("category", applicationConfiguration.getCategory())
            )
        );

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var uniqueIdentifier = applicationConfiguration.getName();
        if (uniqueIdentifier == null) throw new IllegalArgumentException("uniqueIdentifier must be specified.");

        final UpdateBuilder builder = new UpdateBuilder();
        processModifyBuilder.accept(builder);

        final var mongoApplicationConfiguration = getMongoDBUtils().perform(ds ->
            builder.with(
                set("uniqueIdentifier", uniqueIdentifier),
                set("category", applicationConfiguration.getCategory()),
                set("parent", mongoApplication)
            ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId +
                                        " not found for " + applicationNameOrId);
        }

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

        query.filter(and(
           eq("active", true),
           eq("parent", mongoApplication),
           eq("category", category)
        ));

        if (ObjectId.isValid(applicationConfigurationNameOrId)) {
            query.filter(eq("_id", new ObjectId(applicationConfigurationNameOrId)));
        } else {
            query.filter(eq("uniqueIdentifier", applicationConfigurationNameOrId));
        }

        final var mongoApplicationConfiguration = getMongoDBUtils().perform(ds ->
            query.modify(set("active", false))
                 .execute(new ModifyOptions().upsert(false))
        );

        if (mongoApplicationConfiguration == null) {
            throw new NotFoundException("application configuration " + applicationConfigurationNameOrId +
                                        " not found for " + applicationNameOrId);
        }

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

}
