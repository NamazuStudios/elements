package dev.getelements.elements.dao.mongo.system;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.UpdateBuilder;
import dev.getelements.elements.dao.mongo.model.system.MongoElementDeployment;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.system.ElementDeploymentNotFoundException;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.sdk.model.ValidationGroups;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;

import jakarta.inject.Inject;
import java.util.List;
import java.util.Optional;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.updates.UpdateOperators.inc;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;

public class MongoElementDeploymentDao implements ElementDeploymentDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    private BooleanQueryParser booleanQueryParser;

    private ValidationHelper validationHelper;

    @Override
    public ElementDeployment createElementDeployment(final ElementDeployment elementDeployment) {
        getValidationHelper().validateModel(elementDeployment, ValidationGroups.Insert.class);
        final var mongoElementDeployment = getMapperRegistry().map(elementDeployment, MongoElementDeployment.class);
        final var result = getMongoDBUtils().perform(
                ds -> {
                    ds.insert(mongoElementDeployment);
                    return mongoElementDeployment;
                },
                DuplicateException::new
        );
        return getMapperRegistry().map(result, ElementDeployment.class);
    }

    @Override
    public Pagination<ElementDeployment> getElementDeployments(
            final int offset,
            final int count,
            final String search) {

        if (search != null && !search.isBlank()) {
            final var parsed = getBooleanQueryParser()
                    .parse(getDatastore().find(MongoElementDeployment.class), search)
                    .filter(getMongoDBUtils()::isIndexedQuery);
            if (parsed.isPresent()) {
                return getMongoDBUtils().paginationFromQuery(parsed.get(), offset, count, ElementDeployment.class);
            }
        }

        final var query = getDatastore().find(MongoElementDeployment.class);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, ElementDeployment.class);
    }

    @Override
    public Optional<ElementDeployment> findElementDeployment(final String deploymentId) {
        return getMongoDBUtils()
                .parse(deploymentId)
                .map(objectId -> {
                    final var query = getDatastore().find(MongoElementDeployment.class);
                    query.filter(eq("_id", objectId));
                    return query.first();
                })
                .map(result -> getMapperRegistry().map(result, ElementDeployment.class));
    }

    @Override
    public ElementDeployment updateElementDeployment(final ElementDeployment elementDeployment) {
        getValidationHelper().validateModel(elementDeployment, ValidationGroups.Update.class);

        final var objectId = getMongoDBUtils().parseOrThrow(
                elementDeployment.id(),
                ElementDeploymentNotFoundException::new
        );

        final var query = getDatastore()
                .find(MongoElementDeployment.class)
                .filter(eq("_id", objectId));

        final var mongoElementDeployment = getMapperRegistry().map(elementDeployment, MongoElementDeployment.class);

        final var builder = new UpdateBuilder();

        if (mongoElementDeployment.getApiArtifacts() != null) {
            builder.with(set("apiArtifacts", mongoElementDeployment.getApiArtifacts()));
        } else {
            builder.with(unset("apiArtifacts"));
        }

        if (mongoElementDeployment.getSpiArtifacts() != null) {
            builder.with(set("spiArtifacts", mongoElementDeployment.getSpiArtifacts()));
        } else {
            builder.with(unset("spiArtifacts"));
        }

        if (mongoElementDeployment.getElementArtifacts() != null) {
            builder.with(set("elementArtifacts", mongoElementDeployment.getElementArtifacts()));
        } else {
            builder.with(unset("elementArtifacts"));
        }

        if (mongoElementDeployment.getElm() != null) {
            builder.with(set("elm", mongoElementDeployment.getElm()));
        } else {
            builder.with(unset("elm"));
        }

        if (mongoElementDeployment.getElmArtifact() != null) {
            builder.with(set("elmArtifact", mongoElementDeployment.getElmArtifact()));
        } else {
            builder.with(unset("elmArtifact"));
        }

        builder.with(set("useDefaultRepositories", mongoElementDeployment.isUseDefaultRepositories()));

        if (mongoElementDeployment.getRepositories() != null) {
            builder.with(set("repositories", mongoElementDeployment.getRepositories()));
        } else {
            builder.with(unset("repositories"));
        }

        if (mongoElementDeployment.getState() != null) {
            builder.with(set("state", mongoElementDeployment.getState()));
        } else {
            builder.with(unset("state"));
        }

        // Atomically increment version on each update
        builder.with(inc("version", 1L));

        final var opts = new ModifyOptions()
                .upsert(false)
                .returnDocument(AFTER);

        final var result = getMongoDBUtils().perform(
                ds -> builder.execute(query, opts),
                DuplicateException::new
        );

        if (result == null) {
            throw new ElementDeploymentNotFoundException("Element deployment not found: " + elementDeployment.id());
        }

        return getMapperRegistry().map(result, ElementDeployment.class);
    }

    @Override
    public void deleteDeployment(final String deploymentId) {

        final var objectId = getMongoDBUtils().parseOrThrow(
                deploymentId,
                ElementDeploymentNotFoundException::new
        );

        final var query = getDatastore().find(MongoElementDeployment.class);
        query.filter(eq("_id", objectId));

        final var writeResult = query.delete();

        if (writeResult.getDeletedCount() == 0) {
            throw new ElementDeploymentNotFoundException("Element deployment not found: " + deploymentId);
        } else if (writeResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    @Override
    public List<ElementDeployment> getElementDeploymentsByState(final ElementDeploymentState state) {
        final var query = getDatastore().find(MongoElementDeployment.class);
        if (state != null) {
            query.filter(eq("state", state));
        }
        return query.stream()
                .map(m -> getMapperRegistry().map(m, ElementDeployment.class))
                .toList();
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
