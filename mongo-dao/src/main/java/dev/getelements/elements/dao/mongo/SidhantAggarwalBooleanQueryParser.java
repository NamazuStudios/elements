package dev.getelements.elements.dao.mongo;

import com.github.sidhant92.boolparser.domain.*;
import com.github.sidhant92.boolparser.parser.canopy.PEGBoolExpressionParser;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static dev.morphia.query.filters.Filters.*;

/**
 * A {@link BooleanQueryParser} based on Sidhant Aggarwal's boolean query parser.
 * https://github.com/sidhant92/boolparser
 */
public class SidhantAggarwalBooleanQueryParser implements BooleanQueryParser {

    private Datastore datastore;

    @Override
    public <QueryT> Optional<Query<QueryT>> parse(final Class<QueryT> cls, final String query) {
        final var base = getDatastore().find(cls);
        return parse(base, query);
    }

    @Override
    public <QueryT> Optional<Query<QueryT>> parse(final Query<QueryT> base, final String query) {
        final var parser = new PEGBoolExpressionParser();
        return parser.parseExpression(query).map(node -> translate(base, base::filter, node));
    }

    private <QueryT> Query<QueryT> translate(
            final Query<QueryT> base,
            final FilterConsumer consumer,
            final Node node) {
        switch (node.getNodeType()) {
            case STRING_TOKEN:
                return translate(base, consumer, (StringToken)node);
            case NUMERIC_TOKEN:
                return translate(base, consumer, (NumericToken)node);
            case BOOL_EXPRESSION:
                return translate(base, consumer, (BoolExpression)node);
            case NUMERIC_RANGE_TOKEN:
                return translate(base, consumer, (NumericRangeToken)node);
            default:
                throw new UnsupportedOperationException("Unexpected token type: " + node.getNodeType());
        }
    }

    private <QueryT> Query<QueryT> translate(
            final Query<QueryT> base,
            final FilterConsumer consumer,
            final StringToken node) {
        consumer.filter(eq(node.getField(), node.getValue()));
        return base;
    }

    private <QueryT> Query<QueryT> translate(
            final Query<QueryT> base,
            final FilterConsumer consumer,
            final NumericToken node) {

        switch (node.getOperator()) {
            case EQUALS:
                consumer.filter(eq(node.getField(), node.getValue()));
                break;
            case LESS_THAN:
                consumer.filter(lt(node.getField(), node.getValue()));
                break;
            case GREATER_THAN:
                consumer.filter(gt(node.getField(), node.getValue()));
                break;
            case LESS_THAN_EQUAL:
                consumer.filter(lte(node.getField(), node.getValue()));
                break;
            case GREATER_THAN_EQUAL:
                consumer.filter(gte(node.getField(), node.getValue()));
                break;
            default:
                throw new UnsupportedOperationException("Invalid numeric operation:" + node.getOperator());
        }

        return base;

    }

    private <QueryT> Query<QueryT> translate(
            final Query<QueryT> base,
            final FilterConsumer consumer,
            final NumericRangeToken node) {

        consumer.filter(
                gte(node.getField(), node.getFromValue()),
                lte(node.getField(), node.getToValue())
        );

        return base;

    }

    private <QueryT> Query<QueryT> translate(
            final Query<QueryT> base,
            final FilterConsumer consumer,
            final BoolExpression node) {

        final var orFilters = new ArrayList<Filter>();
        final var andFilters = new ArrayList<Filter>();

        final FilterConsumer or = filters -> orFilters.addAll(List.of(filters));
        final FilterConsumer and = filters -> andFilters.addAll(List.of(filters));
        final FilterConsumer not = filters -> Stream.of(filters)
                .map(Filter::not)
                .forEach(consumer::filter);

        node.getOrOperations().forEach(n -> translate(base, or, n));
        node.getAndOperations().forEach(n -> translate(base, and, n));
        node.getNotOperations().forEach(n -> translate(base, not, n));

        if (!orFilters.isEmpty()) consumer.filter(or(orFilters.toArray(Filter[]::new)));
        if (!andFilters.isEmpty()) consumer.filter(and(andFilters.toArray(Filter[]::new)));

        return base;

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    @FunctionalInterface
    private interface FilterConsumer {
        void filter(Filter ... filters);
    }

}
