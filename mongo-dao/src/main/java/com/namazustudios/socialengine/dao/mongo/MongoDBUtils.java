package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.fts.*;
import com.namazustudios.socialengine.model.Pagination;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 6/10/15.
 */
public class MongoDBUtils {

    @Inject
    private Datastore datastore;

    @Inject
    private ObjectIndex objectIndex;

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
        pagination.setTotal((int) query.getCollection().getCount());

        final Iterable<ModelT> userIterable = Iterables.limit(Iterables.transform(query, function) , count);
        pagination.setObjects(Lists.newArrayList(userIterable));
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

            final Iterable<ObjectId> identifiers = Iterables.transform(results,
                    new Function<ScoredDocumentEntry<MongoModelT>, ObjectId>() {
                        @Override
                        public ObjectId apply(ScoredDocumentEntry<MongoModelT> input) {
                            final String objectId = input.getIdentity(kind).getIdentity(String.class);
                            return new ObjectId(objectId);
                        }
                    });

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

}
