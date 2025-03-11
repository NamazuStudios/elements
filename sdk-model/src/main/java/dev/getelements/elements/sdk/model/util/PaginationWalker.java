package dev.getelements.elements.sdk.model.util;

import dev.getelements.elements.sdk.model.Pagination;

import java.util.ArrayList;
import java.util.List;
import java.util.function.BiFunction;
import java.util.function.Consumer;

/**
 * Walks set of data that is managed through a {@link Pagination}.
 */
public class PaginationWalker {

    private int count = 20;

    private int offset = 0;

    private int total = Integer.MAX_VALUE;

    /**
     * Specifies the count on each page fetch.
     *
     * @param count the count
     * @return this instance
     */
    public PaginationWalker withCount(int count) {
        this.count = offset;
        return this;
    }

    /**
     * Specifies the offset on each page fetch.
     *
     * @param offset the offset
     * @return this instance
     */
    public PaginationWalker withOffset(int offset) {
        this.offset = offset;
        return this;
    }


    /**
     * Specifies the maximum total to fetch
     *
     * @param total the total
     * @return this instance
     */
    public PaginationWalker withTotal(int total) {
        this.total = total;
        return this;
    }

    /**
     * Aggregates the result of many paginations.
     *
     * @param initial the initial aggregate value
     * @param walkFunction the walk function
     * @param aggregatorFunction the aggregator function
     * @param <PaginatedT> the type which is paginated
     * @param <AggregateT> the aggregate type
     * @return the aggregate
     */
    public <PaginatedT, AggregateT> AggregateT aggregate(
            final AggregateT initial,
            final WalkFunction<PaginatedT> walkFunction,
            final BiFunction<AggregateT, Pagination<PaginatedT>, AggregateT> aggregatorFunction) {
        var aggregate = initial;
        var page = walkFunction.getPage(offset, count);

        aggregate = aggregatorFunction.apply(aggregate, page);

        for (int offset = this.offset + page.getObjects().size();
             offset < total && offset < page.getTotal();
             offset += page.getObjects().size()) {
            page = walkFunction.getPage(offset, count);
            aggregate = aggregatorFunction.apply(aggregate, page);
        }

        return aggregate;
    }

    /**
     * Aggregates several pages to a list.
     *
     * @param walkFunction the walk function
     * @param <PaginatedT> the paginated type
     * @return a {@link List} with all known entries
     */
    public <PaginatedT> List<PaginatedT> toList(final WalkFunction<PaginatedT> walkFunction) {
        return aggregate(new ArrayList<>(), walkFunction, (l, p) -> {
            l.addAll(p.getObjects());
            return l;
        });
    }

    /**
     * Walks a dataset passing each object from each page into the
     * @param walkFunction
     * @param tConsumer
     * @param <PaginatedT>
     */
    public <PaginatedT> void forEach(final WalkFunction<PaginatedT> walkFunction,
                                     final Consumer<PaginatedT> tConsumer) {
        aggregate(null, walkFunction, (o, p) -> {
            p.forEach(tConsumer);
            return o;
        });
    }

    /**
     * Used to fetch the next page of a collection.
     *
     * @param <T>
     */
    @FunctionalInterface
    public interface WalkFunction<T> {

        /**
         * Fetches the next page.
         *
         * @param offset the offset
         * @param count the count
         * @return the {@link Pagination} with the results for the page
         */
        Pagination<T> getPage(final int offset, int count);

    }

}
