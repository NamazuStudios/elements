package dev.getelements.elements.dao.mongo.application;

import com.mongodb.MongoCommandException;
import com.namazustudios.elements.fts.ObjectIndex;
import dev.getelements.elements.dao.ApplicationDao;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.exception.*;
import dev.getelements.elements.exception.application.ApplicationNotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.experimental.filters.Filters.and;
import static dev.morphia.query.experimental.filters.Filters.eq;
import static dev.morphia.query.experimental.updates.UpdateOperators.set;

/**
 * Created by patricktwohig on 7/10/15.
 */
public class MongoApplicationDao implements ApplicationDao {

    @Inject
    private ValidationHelper validationHelper;

    @Inject
    private ObjectIndex objectIndex;

    @Inject
    private StandardQueryParser standardQueryParser;

    @Inject
    private MongoDBUtils mongoDBUtils;

    @Inject
    private Datastore datastore;

    @Override
    public Application createOrUpdateInactiveApplication(final Application application) {

        validate(application);

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        query.filter(
            and(
                eq("name", application.getName()),
                eq("active", false)
            )
        );

        final var mongoApplication = mongoDBUtils.perform(ds ->
            query.modify(
                set("name", application.getName().trim()),
                set("description", nullToEmpty(application.getDescription()).trim()),
                set("active", true)
            ).execute(new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        objectIndex.index(mongoApplication);
        return transform(mongoApplication);

    }

    @Override
    public Pagination<Application> getActiveApplications() {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(eq("active", true));

        final List<Application> applicationList = query.iterator().toList()
            .stream()
            .map(this::transform)
            .collect(Collectors.toList());

        final Pagination<Application> applicationPagination = new Pagination<>();
        applicationPagination.setApproximation(false);
        applicationPagination.setObjects(applicationList);
        applicationPagination.setTotal(applicationList.size());

        return applicationPagination;

    }

    @Override
    public Pagination<Application> getActiveApplications(int offset, int count) {
        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(eq("active", true));
        return mongoDBUtils.paginationFromQuery(query, offset, count, this::transform, new FindOptions());
    }

    @Override
    public Pagination<Application> getActiveApplications(int offset, int count, String search) {

        final var booleanQueryBuilder = new BooleanQuery.Builder();

        try {

            final var activeTerm = new Term("active", "true");

            booleanQueryBuilder.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(standardQueryParser.parse(search, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return mongoDBUtils.paginationFromSearch(
            MongoApplication.class,
            booleanQueryBuilder.build(),
            offset, count,
            this::transform);

    }

    @Override
    public Application getActiveApplication(String nameOrId) {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        query.filter(eq("active", true));

        try {
            query.filter(eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("name", nameOrId));
        }

        final MongoApplication mongoApplication = query.first();

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("Application " + nameOrId + " not found.");
        }

        return transform(mongoApplication);

    }

    @Override
    public Application updateActiveApplication(String nameOrId, Application application) {

        validate(application);

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        query.filter(eq("active", true));

        try {
            query.filter(eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(eq("name", nameOrId));
        }

        final var mongoApplication = mongoDBUtils.perform(ds ->
            query.modify(
                set("name", application.getName().trim()),
                set("description", nullToEmpty(application.getDescription()).trim()),
                set("active", true)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoApplication == null) {
            throw new NotFoundException("application not found: " + nameOrId);
        }

        objectIndex.index(mongoApplication);
        return transform(mongoApplication);

    }

    @Override
    public void softDeleteApplication(String nameOrId) {

        final var query = datastore.find(MongoApplication.class);

        if (ObjectId.isValid(nameOrId)) {
            query.filter(
                and(
                    eq("_id", new ObjectId(nameOrId)),
                    eq("active", true)
                )
            );
        } else {
            query.filter(
                and(
                    eq("_id", new ObjectId(nameOrId)),
                    eq("name", nameOrId)
                )
            );
        }

        final var mongoApplication = mongoDBUtils.perform(ds ->
            query.modify(
                set("active", false)
            ).execute(new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoApplication == null) {
            throw new NotFoundException("application not found: " + nameOrId);
        }

        objectIndex.index(mongoApplication);

    }

    public MongoApplication findActiveMongoApplication(final String mongoApplicationNameOrId) {

        final var query = datastore.find(MongoApplication.class);

        if (ObjectId.isValid(mongoApplicationNameOrId)) {
            query.filter(and(
                eq("_id", new ObjectId(mongoApplicationNameOrId)),
                eq("active", true)
            ));
        } else {
            query.filter(and(
                eq("name", mongoApplicationNameOrId),
                eq("active", true)
            ));
        }

        return query.first();

    }

    public MongoApplication getActiveMongoApplication(final String mongoApplicationNameOrId) {

        final MongoApplication mongoApplication = findActiveMongoApplication(mongoApplicationNameOrId);

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("application not found: " + mongoApplicationNameOrId);
        }

        return mongoApplication;

    }

    public Application transform(final MongoApplication mongoApplication) {

        final Application application = new Application();

        if (mongoApplication.getObjectId() != null) {
            application.setId(mongoApplication.getObjectId().toHexString());
        }

        application.setName(mongoApplication.getName());
        application.setDescription(mongoApplication.getDescription());

        return application;

    }

    public void validate(final Application application) {

        if (application == null) {
            throw new InvalidDataException("application must not be null.");
        }

        validationHelper.validateModel(application);

    }

}
