package com.namazustudios.socialengine.dao.mongo;

import com.google.common.collect.Iterables;
import com.mongodb.MongoCommandException;
import com.namazustudios.socialengine.Constants;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.elements.fts.NoResultException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.elements.fts.SearchException;
import com.namazustudios.elements.fts.TopDocsSearchResult;
import com.namazustudios.socialengine.model.Pagination;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.FindOptions;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.lang.Math.min;
import static java.util.stream.StreamSupport.stream;

/**
 * Some helper methods used in various parts of the MongoDB code.
 *
 * Created by patricktwohig on 6/10/15.
 */
public class MongoDBUtils {

    private AdvancedDatastore datastore;

    private ObjectIndex objectIndex;

    private int queryMaxResults;

    /**
     * Performs the supplied operation, catching all {@link MongoCommandException} instances and
     * mapping to the appropraite type of exception internally.
     *
     * @param operation the operation
     * @param <T> the expected return type
     * @return the object retured by the supplied operation
     */
    public <T> T perform(final Function<AdvancedDatastore, T> operation) {
        try {
            return operation.apply(getDatastore());
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }
    }

    /**
     * Parses the given ObjectID string using {@link ObjectId}.  If this fails, this throws the appropriate exception
     * type of {@link NotFoundException}.
     *
     * @param objectId the object ID to parse
     * @return an {@link ObjectId} (never null)
     */
    public ObjectId parseOrThrowNotFoundException(final String objectId) {
        try {
            return new ObjectId(objectId);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException("Object with ID " + objectId + " not found.");
        }
    }

    /**
     * Transforms the given {@link Query} to the resulting {@link Pagination}.
     *
     * @param query the query
     * @param offset the offset
     * @param count the count
     * @param function the function to transform the values
     * @param <ModelT> the desired model type
     * @param <MongoModelT> the mongoDB model type
     * @return a {@link Pagination} instance for the given ModelT
     */
    public <ModelT, MongoModelT> Pagination<ModelT> paginationFromQuery(
            final Query<MongoModelT> query, final int offset, final int count,
            final Function<MongoModelT,  ModelT> function) {

        final Pagination<ModelT> pagination = new Pagination<>();

        pagination.setOffset(offset);
        pagination.setTotal((int) query.count());

        final int limit = min(queryMaxResults, count);

        final List<ModelT> modelTList = query.asList(new FindOptions().skip(offset))
            .stream()
            .map(function)
            .limit(limit)
            .collect(Collectors.toList());

        pagination.setObjects(modelTList);
        return pagination;

    }

    /**
     * Generates a MongoDB query given the lucene query
     *
     * @param kind the kind, or mongo Java type to search
     * @param searchQuery the search query itself.
     * @param offset the offset
     * @param count the count
     * @param <MongoModelT> the Mongo model type (aka Kind)
     *
     * @return the Query instance which can be used to fetch the search results
     */
    public <MongoModelT> Query<MongoModelT> queryForSearch(
            final Class<MongoModelT> kind,
            final org.apache.lucene.search.Query searchQuery,
            final int offset, final int count) {

        final Query<MongoModelT> mongoQuery = datastore.createQuery(kind);

        try (final TopDocsSearchResult<MongoModelT> results = objectIndex
                .executeQueryForObjects(kind, searchQuery)
                .withTopScores(count + offset)
                .after(offset, count)) {

            final Iterable<ObjectId> identifiers;
            identifiers = Iterables.transform(results, input -> input.getIdentity(kind).getIdentity(ObjectId.class));
            mongoQuery.criteria("_id").in(identifiers);

        }

        return mongoQuery;

    }

    /**
     * Combines the functionality of {@link #queryForSearch(Class, org.apache.lucene.search.Query, int, int)} with
     * the functionality of {@link #paginationFromQuery(Query, int, int, Function)} together to simplify
     * searching for objects.
     *
     * @param kind the kind to search
     * @param searchQuery the search query
     * @param offset the offset
     * @param count the count
     * @param function the function to transform the values {@see {@link #paginationFromQuery(Query, int, int, Function)}}
     * @param <ModelT>
     * @param <MongoModelT>
     * @return the pagination object
     */
    public <ModelT, MongoModelT> Pagination<ModelT> paginationFromSearch(
            final Class<MongoModelT> kind,
            final org.apache.lucene.search.Query searchQuery,
            final int offset, final int count,
            final Function<MongoModelT,  ModelT> function) {
        try {
            final Query<MongoModelT> mongoQuery = queryForSearch(kind, searchQuery, offset, count);
            final Pagination<ModelT> pagination = paginationFromQuery(mongoQuery, offset, count, function);
            pagination.setApproximation(true);
            return pagination;
        } catch (NoResultException ex) {
            final Pagination<ModelT> pagination = new Pagination<>();
            pagination.setApproximation(true);
            return pagination;
        } catch (SearchException ex) {
            throw new InternalException(ex.getMessage(), ex);
        }
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public int getQueryMaxResults() {
        return queryMaxResults;
    }

    @Inject
    public void setQueryMaxResults(    @Named(Constants.QUERY_MAX_RESULTS) int queryMaxResults) {
        this.queryMaxResults = queryMaxResults;
    }

}
