package com.namazustudios.socialengine.dao.mongo.application;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mongodb.MongoCommandException;
import com.mongodb.client.result.UpdateResult;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.exception.application.ApplicationNotFoundException;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;

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

        query.filter(Filters.and(
                Filters.eq("name", application.getName()),
                Filters.eq("active", false)
        ));

        final UpdateOperations<MongoApplication> updateOperations = datastore.createUpdateOperations(MongoApplication.class);

        final UpdateResult updateResult = query.update(UpdateOperators.set("name", application.getName().trim()),
                UpdateOperators.set("description", Strings.nullToEmpty(application.getDescription()).trim()),
                UpdateOperators.set("active", true)
                ).execute(new UpdateOptions().upsert(true));

        final MongoApplication mongoApplication;

        try {
            if(updateResult.getUpsertedId() != null){
                mongoApplication = datastore.find(MongoApplication.class)
                        .filter(Filters.eq("_id", updateResult.getUpsertedId())).first();
            }
            else{
                mongoApplication = datastore.find(MongoApplication.class)
                        .filter(Filters.and(
                                Filters.eq("name", application.getName()),
                                Filters.eq("active", true)
                        )).first();
            }
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        objectIndex.index(mongoApplication);
        return transform(mongoApplication);

    }

    @Override
    public Pagination<Application> getActiveApplications() {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);
        query.filter(Filters.eq("active", true));

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
        query.filter(Filters.eq("active", true));

        return mongoDBUtils.paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());

    }

    @Override
    public Pagination<Application> getActiveApplications(int offset, int count, String search) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {

            final Term activeTerm = new Term("active", "true");

            booleanQueryBuilder.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(standardQueryParser.parse(search, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return mongoDBUtils.paginationFromSearch(MongoApplication.class, booleanQueryBuilder.build(), offset, count, (Function<MongoApplication, Application>) input -> transform(input));
    }

    @Override
    public Application getActiveApplication(String nameOrId) {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        query.filter(Filters.eq("active", true));

        try {
            query.filter(Filters.eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("name", nameOrId));
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

        query.filter(Filters.eq("active", true));

        try {
            query.filter(Filters.eq("_id", new ObjectId(nameOrId)));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.eq("name", nameOrId));
        }

        query.update(UpdateOperators.set("name", application.getName().trim()),
                UpdateOperators.set("description", Strings.nullToEmpty(application.getDescription()).trim()),
                UpdateOperators.set("active", true)
                ).execute(new UpdateOptions().upsert(false));

        final MongoApplication mongoApplication;

        try {
            mongoApplication = query.first();
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoApplication == null) {
            throw new NotFoundException("application not found: " + nameOrId);
        }

        objectIndex.index(mongoApplication);
        return transform(mongoApplication);

    }

    @Override
    public void softDeleteApplication(String nameOrId) {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        try {
            query.filter(Filters.and(
                    Filters.eq("_id", new ObjectId(nameOrId)),
                    Filters.eq("active", true)
            ));
        } catch (IllegalArgumentException ex) {
            query.filter(Filters.and(
                    Filters.eq("name", nameOrId),
                    Filters.eq("active", true)
            ));
        }

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoApplication mongoApplication;

        try {
            final Query<MongoApplication> qry = datastore.find(MongoApplication.class);
            try {
                qry.filter(Filters.and(
                        Filters.eq("_id", new ObjectId(nameOrId)),
                        Filters.eq("active", false)
                ));
            } catch (IllegalArgumentException ex) {
                qry.filter(Filters.and(
                        Filters.eq("name", nameOrId),
                        Filters.eq("active", false)
                ));
            }
            mongoApplication = qry.first();
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (mongoApplication == null) {
            throw new NotFoundException("application not found: " + nameOrId);
        }

        objectIndex.index(mongoApplication);

    }

    public MongoApplication findActiveMongoApplication(final String mongoApplicationNameOrId) {

        final Query<MongoApplication> query = datastore.find(MongoApplication.class);

        if (ObjectId.isValid(mongoApplicationNameOrId)) {
            query.filter(Filters.and(
                    Filters.eq("_id", new ObjectId(mongoApplicationNameOrId)),
                    Filters.eq("active", true)
            ));
        } else {
            query.filter(Filters.and(
                    Filters.eq("name", mongoApplicationNameOrId),
                    Filters.eq("active", true)
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
