package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.UpdateResult;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.Modify;
import dev.morphia.query.Query;
import dev.morphia.query.Update;
import dev.morphia.query.updates.UpdateOperator;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.function.Function;

import static java.util.Arrays.asList;

public class UpdateBuilder {

    private final List<UpdateOperator> updates = new ArrayList<>();

    /**
     * Constructs an empty {@link UpdateBuilder}.
     */
    public UpdateBuilder() {}

    /**
     * Constructs an {@link UpdateBuilder} with the supplied updates.
     *
     * @param updates the updates to apply
     */
    public UpdateBuilder(final Collection<UpdateOperator> updates) {
        this.updates.addAll(updates);
    }

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
     * Accepts an {@link Function} which consumes this {@link UpdateBuilder} and returns the value
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
     * Builds the {@link Modify} from the supplied {@link Query}.
     *
     * @param query the {@link Query}
     * @param <ModelT> the model type to update
     * @return an instance of {@link Modify} with the updates supplied by this builder
     *
     * @throws IllegalStateException if no updates were applied.
     */
    public <ModelT> Modify<ModelT> modify(final Query<ModelT> query) {

        if (updates.isEmpty()) throw new IllegalStateException("Must specify at last one update.");

        final var first = updates.get(0);

        final var remaining = updates.size() > 1
            ? updates.subList(1, updates.size()).toArray(UpdateOperator[]::new)
            : new UpdateOperator[0];

        return query.modify(first, remaining);

    }

    /**
     * Builds the {@link Update} from the supplied {@link Query}.
     *
     * @param query the {@link Query}
     * @param <ModelT> the model type to update
     * @return an instance of {@link Update} with the updates supplied by this builder
     * @deprecated depends on deprecated code and is slated for removal
     * @throws IllegalStateException if no updates were applied.
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
     * Builds a new {@link UpdateBuilder} based on this instance.
     *
     * @return a new {@link UpdateBuilder} instanc
     */
    public UpdateBuilder then() {
        return new UpdateBuilder(updates);
    }

    /**
     * Equivalent to invoking {@link #update(Query)} (Query)}.{@link Update#execute(UpdateOptions)}
     * @param query the {@link Query} against which to apply the updates
     *
     * @return the {@link UpdateResult}
     */
    public UpdateResult execute(final Query<?> query, final UpdateOptions options) {
        return query.update(options, updates.toArray(UpdateOperator[]::new));
    }

    /**
     * Equivalent to invoking {@link #modify(Query)}. {@link Modify#execute(ModifyOptions)}
     * @param query the {@link Query} against which to apply the updates
     *
     * @return the model
     */
    public <ModelT> ModelT execute(final Query<ModelT> query, final ModifyOptions options) {
        return query.modify(options, updates.toArray(UpdateOperator[]::new));
    }

}
