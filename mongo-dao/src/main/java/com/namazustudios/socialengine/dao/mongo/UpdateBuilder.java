package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Query;
import dev.morphia.query.Update;
import dev.morphia.query.experimental.updates.UpdateOperator;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

/**
 * Builds a batch update for a {@link Query<?>}.
 */
public class UpdateBuilder {

    private final List<UpdateOperator> updates = new ArrayList<>();

    /**
     * Appends an {@link UpdateOperator}.
     *
     * @param op the operator
     * @return this instance
     */
    public UpdateBuilder with(final UpdateOperator op) {
        updates.add(op);
        return this;
    }

    /**
     * Appends an {@link UpdateOperator}.
     *
     * @param first the first operator to apply
     * @param subsequent the subsequent operators to apply
     * @return this instance
     */
    public UpdateBuilder with(final UpdateOperator first, final UpdateOperator ... subsequent) {
        updates.add(first);
        updates.addAll(asList(subsequent));
        return this;
    }

    /**
     * Accepts an {@link Function<UpdateBuilder, T>} which consumes this {@link UpdateBuilder} and returns the value
     * returned by the function.
     *
     * @param op the operation to apply to this {@link UpdateBuilder}
     * @param <T> the return type.
     * @return the value returned from the operation
     */
    public <T> T with(final Function<UpdateBuilder, T> op) {
         return op.apply(this);
    }

    /**
     * Builds the {@link Update<ModelT>} from the supplied {@link Query<ModelT>}.
     *
     * @param query the {@link Query<ModelT>}
     * @param <ModelT> the model type to update
     * @return an instance of {@link Update<ModelT>} with the updates supplied by this builder
     *
     * @throws {@link IllegalStateException} if no updates were applied.
     */
    public <ModelT> Update<ModelT> update(final Query<ModelT> query) {

        if (updates.isEmpty()) throw new IllegalStateException("Must specify at last one update.");

        final var first = updates.get(0);

        final var remaining = updates.size() > 1
            ? updates.subList(1, updates.size()).toArray(UpdateOperator[]::new)
            : new UpdateOperator[0];

        return query.update(first, remaining);

    }

    /**
     * Equivalent to invoking {@link #update(Query)}.{@link Update#execute()}
     * @param query the {@link Query<?>} against which to apply the updates
     *
     * @return the {@link UpdateResult}
     */
    public UpdateResult execute(final Query<?> query) {
        return update(query).execute();
    }

    /**
     * Equivalent to invoking {@link #update(Query)}.{@link Update#execute(UpdateOptions)}
     * @param query the {@link Query<?>} against which to apply the updates
     *
     * @return the {@link UpdateResult}
     */
    public UpdateResult execute(final Query<?> query, final UpdateOptions options) {
        return update(query).execute(options);
    }

}
