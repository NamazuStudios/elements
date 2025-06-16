package dev.getelements.elements.dao.mongo.application;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static java.util.Collections.emptyMap;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class MongoApplicationDao implements ApplicationDao {

    private ValidationHelper validationHelper;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry mapperRegistry;

    @Override
    public Application createOrUpdateInactiveApplication(final Application application) {

        validate(application, ValidationGroups.Insert.class);

        final var mongoApplication = getMapperRegistry().map(application, MongoApplication.class);
        getDatastore().insert(mongoApplication);

        return transform(mongoApplication);

    }

    @Override
    public Pagination<Application> getActiveApplications() {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(exists("name"));

        final List<Application> applicationList;

        try (final var iterator = query.iterator()) {
            applicationList = iterator.toList()
                    .stream()
                    .map(this::transform)
                    .collect(Collectors.toList());
        }

        final Pagination<Application> applicationPagination = new Pagination<>();
        applicationPagination.setApproximation(false);
        applicationPagination.setObjects(applicationList);
        applicationPagination.setTotal(applicationList.size());

        return applicationPagination;

    }

    @Override
    public Pagination<Application> getActiveApplications(final int offset, final int count) {
        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(exists("name"));
        return mongoDBUtils.paginationFromQuery(query, offset, count, this::transform, new FindOptions());
    }

    @Override
    public Pagination<Application> getActiveApplications(final int offset, final int count, final String search) {
        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(exists("name"));
        return mongoDBUtils.paginationFromQuery(query, offset, count, this::transform, new FindOptions());
    }

    @Override
    public Optional<Application> findActiveApplication(final String nameOrId) {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        query.filter(exists("name"));

        try {
            query.filter(eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("name", nameOrId));
        }

        final MongoApplication mongoApplication = query.first();

        return Optional.ofNullable(mongoApplication).map(this::transform);

    }

    public Application getActiveApplicationWithoutAttributes(final String nameOrId) {
        Application application = getActiveApplication(nameOrId);
        application.setAttributes(emptyMap());
        return application;
    }

    @Override
    public Application updateActiveApplication(final String nameOrId, final Application application) {

        validate(application, ValidationGroups.Update.class);

        final var query = datastore.find(MongoApplication.class);

        query.filter(exists("name"));

        if (ObjectId.isValid(nameOrId)) {
            query.filter(eq("_id", new ObjectId(nameOrId)));
        } else {
            query.filter(eq("name", nameOrId));
        }

        final var mongoApplication = mongoDBUtils.perform(ds -> new UpdateBuilder().with(
                set("name", application.getName().trim()),
                set("description", nullToEmpty(application.getDescription()).trim()),
                set("attributes", application.getAttributes())
        ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER)));

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("application not found: " + nameOrId);
        }

        return transform(mongoApplication);

    }

    @Override
    public void softDeleteApplication(final String nameOrId) {

        final var query = datastore.find(MongoApplication.class);

        if (ObjectId.isValid(nameOrId)) {
            query.filter(
                and(
                    eq("_id", new ObjectId(nameOrId)),
                    exists("name")
                )
            );
        } else {
            query.filter(
                and(
                    eq("_id", new ObjectId(nameOrId)),
                    exists("name")
                )
            );
        }

        final var mongoApplication = mongoDBUtils.perform(ds -> new UpdateBuilder().with(
                unset("name")
        ).execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER)));

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("application not found: " + nameOrId);
        }

    }

    public MongoApplication findMongoApplication(final String mongoApplicationNameOrId) {

        final var query = datastore.find(MongoApplication.class);

        if (ObjectId.isValid(mongoApplicationNameOrId)) {
            query.filter(and(
                    eq("_id", new ObjectId(mongoApplicationNameOrId))
            ));
        } else {
            query.filter(and(
                    eq("name", mongoApplicationNameOrId)
            ));
        }

        return query.first();

    }

    public Optional<MongoApplication> findActiveMongoApplication(final String mongoApplicationNameOrId) {
        return getMongoDBUtils().parse(mongoApplicationNameOrId)
                .map(objectId -> getDatastore()
                        .find(MongoApplication.class)
                        .filter(eq("_id", objectId))
                ).orElseGet(() -> getDatastore()
                        .find(MongoApplication.class)
                        .filter(eq("name", mongoApplicationNameOrId))
                )
                .stream()
                .findFirst();
    }

    public MongoApplication getActiveMongoApplication(final String mongoApplicationNameOrId) {

        final MongoApplication mongoApplication = findActiveMongoApplication(mongoApplicationNameOrId);

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("application not found: " + mongoApplicationNameOrId);
        }

        return mongoApplication;

    }

    private Application transform(final MongoApplication mongoApplication) {
        return getMapperRegistry().map(mongoApplication, Application.class);
    }

    private void validate(final Application application, final Class<?> group) {

        if (application == null) {
            throw new InvalidDataException("application must not be null.");
        }

        validationHelper.validateModel(application, group);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

}
