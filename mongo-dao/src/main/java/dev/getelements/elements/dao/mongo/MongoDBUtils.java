package dev.getelements.elements.dao.mongo;

import com.mongodb.MongoCommandException;
import com.mongodb.MongoWriteException;
import dev.getelements.elements.Constants;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Sort;
import org.bson.Document;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Function;

import static dev.morphia.query.Sort.ascending;
import static java.lang.Math.min;
import static java.util.stream.Collectors.toList;

/**
 * Some helper methods used in various parts of the MongoDB code.
 *
 * Created by patricktwohig on 6/10/15.
 */

public class MongoDBUtils {

    public static final String COLLSCAN = "COLLSCAN";

    private Datastore datastore;

    private int queryMaxResults;

    private Mapper mapper;

    /**
     * Performs the supplied operation, catching all {@link MongoCommandException} instances and
     * mapping to the appropriate type of exception internally.
     *
     * @param operation the operation
     * @param <T> the expected return type
     * @return the object retured by the supplied operation
     */
    public <T> T perform(final Function<Datastore, T> operation) {
        return perform(operation, DuplicateException::new);
    }

    /**
     * Performs the supplied operation, catching all {@link MongoCommandException} instances and
     * mapping to the appropriate type of exception internally.
     *
     * @param operation the operation
     * @return the object retured by the supplied operation
     */
    public void performV(final Consumer<Datastore> operation) {
        perform(ds -> {
            operation.accept(ds);
            return null;
        }, DuplicateException::new);
    }

    /**
     * Performs the supplied operation, catching all {@link MongoCommandException} instances and
     * mapping to the appropraite type of exception internally.
     *
     * @param operation the operation
     * @param <T> the expected return type
     * @return the object retured by the supplied operation
     */
    public <T, ExceptionT extends Throwable> T perform(
            final Function<Datastore, T> operation,
            final Function<Throwable, ExceptionT> exceptionTSupplier) throws ExceptionT {
        try {
            return operation.apply(getDatastore());
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw exceptionTSupplier.apply(ex);
            } else {
                throw new InternalException(ex);
            }
        } catch (MongoWriteException ex) {
            if (ex.getCode() == 11000) {
                throw exceptionTSupplier.apply(ex);
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
     *
     * @deprecated Use {@link #parseOrThrow(String, Function)}
     */
    @Deprecated
    public ObjectId parseOrThrowNotFoundException(final String objectId) {
        return parseOrThrow(objectId, NotFoundException::new);
    }

    /**
     * Parses the given ObjectID string using {@link ObjectId}.  If this fails, this throws the appropriate exception
     * type of {@link NotFoundException}.
     *
     * @param objectId the object ID to parse
     * @return an {@link ObjectId} (never null)
     */
    public <ExceptionT extends NotFoundException>
    ObjectId parseOrThrow(final String objectId,
                          final Function<String, ExceptionT> exceptionTSupplier) throws ExceptionT {

        final ObjectId oid = parseOrReturnNull(objectId);

        if (oid == null) {
            throw exceptionTSupplier.apply("Object with ID " + objectId + " not found.");
        }

        return new ObjectId(objectId);

    }

    /**
     * Tries to parse a string into an {@link ObjectId}.
     *
     * @param idString the id string
     * @return the {@link Optional<ObjectId>}
     */
    public Optional<ObjectId> parse(final String idString) {
        return Optional.ofNullable(parseOrReturnNull(idString));
    }

    /**
     * Parses the given ObjectID string using {@link ObjectId}.  If this fails, this throws the appropriate exception
     * type of {@link NotFoundException}.
     *
     * @param objectId the object ID to parse
     * @return an {@link ObjectId} (never null)
     */
    public ObjectId parseOrReturnNull(final String objectId) {
        return objectId == null || !ObjectId.isValid(objectId) ? null : new ObjectId(objectId);
    }

    /**
     * Transforms the given {@link Query} to the resulting {@link Pagination}.
     *
     * @param query the query
     * @param offset the offset
     * @param count the count
     * @param modelTClass the destination Class
     * @param <ModelT> the desired model type
     * @param <MongoModelT> the mongoDB model type
     * @return a {@link Pagination} instance for the given ModelT
     */
    public <ModelT, MongoModelT> Pagination<ModelT> paginationFromQuery(
            final Query<MongoModelT> query, final int offset, final int count,
            final Class<ModelT> modelTClass) {
        return paginationFromQuery(query, offset, count, o -> getMapper().map(o, modelTClass), new FindOptions());
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
        return paginationFromQuery(query, offset, count, function, new FindOptions());
    }

    /**
     * Transforms the given {@link Query} to the resulting {@link Pagination}.
     *
     * @param query the query
     * @param offset the offset
     * @param count the count
     * @param function the function to transform the values
     * @param options a {@link FindOptions} used to modify the query results
     * @param <ModelT> the desired model type
     * @param <MongoModelT> the mongoDB model type
     * @return a {@link Pagination} instance for the given ModelT
     */
    public <ModelT, MongoModelT> Pagination<ModelT> paginationFromQuery(
            final Query<MongoModelT> query, final int offset, final int count,
            final Function<MongoModelT,  ModelT> function, final FindOptions options) {


        final Pagination<ModelT> pagination = new Pagination<>();

        pagination.setOffset(offset);
        pagination.setTotal((int) query.count());

        final int limit = min(getQueryMaxResults(), count);

        options.skip(offset);
        options.limit(limit);
        options.sort(ascending("_id"));

        final List<ModelT> modelTList;

        try (final var iterator = query.iterator(options)) {
            modelTList = iterator
                .toList()
                .stream()
                .map(function)
                .collect(toList());
        }

        pagination.setObjects(modelTList);
        return pagination;

    }

    public boolean isIndexedQuery(final Query<?> query) {
        final var planner = (Document) query.explain().get("queryPlanner");
        final var winner = planner.get("winningPlan", Document.class);
        return isIndexedPlan(winner);
    }

    public boolean isIndexedPlan(final Document plan) {

        final var stage = plan.get("stage", String.class);
        final var inputStage = plan.get("inputStage", Document.class);
        final var inputStages = plan.getList("inputStages", Document.class, List.of());

        return !COLLSCAN.equals(stage)
                && (inputStage == null || isIndexedPlan(inputStage))
                && inputStages
                    .stream()
                    .map(this::isIndexedPlan)
                    .reduce(true, (a, b) -> a && b);

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public int getQueryMaxResults() {
        return queryMaxResults;
    }

    @Inject
    public void setQueryMaxResults(    @Named(Constants.QUERY_MAX_RESULTS) int queryMaxResults) {
        this.queryMaxResults = queryMaxResults;
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

}
