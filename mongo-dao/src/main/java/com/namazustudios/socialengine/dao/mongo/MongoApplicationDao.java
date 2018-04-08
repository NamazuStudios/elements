package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.util.ValidationHelper;
import com.namazustudios.socialengine.dao.ApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.Application;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

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
    private AdvancedDatastore datastore;

    @Override
    public Application createOrUpdateInactiveApplication(final Application application) {

        validate(application);

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);

        query.and(
                query.criteria("name").equal(application.getName()),
                query.criteria("active").equal(false)
        );

        final UpdateOperations<MongoApplication> updateOperations = datastore.createUpdateOperations(MongoApplication.class);

        updateOperations.set("name", application.getName().trim());
        updateOperations.set("description", Strings.nullToEmpty(application.getDescription()).trim());
        updateOperations.set("active", true);

        final MongoApplication mongoApplication;

        try {
            mongoApplication = datastore.findAndModify(query, updateOperations, false, true);
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

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);
        query.filter("active = ", true);

        final List<Application> applicationList = query.asList()
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

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);
        query.filter("active = ", true);

        return mongoDBUtils.paginationFromQuery(query, offset, count, input -> transform(input));

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

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);

        query.filter("active =", true);

        try {
            query.filter("_id", new ObjectId(nameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", nameOrId);
        }

        final MongoApplication mongoApplication = query.get();

        if (mongoApplication == null) {
            throw new ApplicationNotFoundException("Application " + nameOrId + " not found.");
        }

        return transform(mongoApplication);

    }

    @Override
    public Application updateActiveApplication(String nameOrId, Application application) {

        validate(application);

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);

        query.filter("active =", true);

        try {
            query.filter("_id", new ObjectId(nameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", nameOrId);
        }

        final UpdateOperations<MongoApplication> updateOperations = datastore.createUpdateOperations(MongoApplication.class);

        updateOperations.set("name", application.getName().trim());
        updateOperations.set("description", Strings.nullToEmpty(application.getDescription()).trim());
        updateOperations.set("active", true);

        final MongoApplication mongoApplication;

        try {
            mongoApplication = datastore.findAndModify(query, updateOperations, false, false);
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

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);

        query.filter("active =", true);

        try {
            query.filter("_id", new ObjectId(nameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", nameOrId);
        }

        final UpdateOperations<MongoApplication> updateOperations = datastore.createUpdateOperations(MongoApplication.class);
        updateOperations.set("active", false);

        final MongoApplication mongoApplication;

        try {
            mongoApplication = datastore.findAndModify(query, updateOperations, false, false);
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

    public MongoApplication getActiveMongoApplication(final String mongoApplicationNameOrId) {

        final Query<MongoApplication> query = datastore.createQuery(MongoApplication.class);

        query.filter("active =", true);

        try {
            query.filter("_id", new ObjectId(mongoApplicationNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", mongoApplicationNameOrId);
        }

        final MongoApplication mongoApplication = query.get();

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
