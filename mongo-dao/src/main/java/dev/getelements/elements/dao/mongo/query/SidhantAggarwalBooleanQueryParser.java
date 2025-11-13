package dev.getelements.elements.dao.mongo.query;

import com.github.sidhant92.boolparser.domain.*;
import com.github.sidhant92.boolparser.parser.canopy.PEGBoolExpressionParser;
import dev.morphia.Datastore;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filter;

import jakarta.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Stream;

import static dev.getelements.elements.dao.mongo.query.BooleanQueryOperator.DEFAULT;
import static dev.morphia.query.filters.Filters.*;
import static java.lang.Double.NaN;

/**
 * A {@link BooleanQueryParser} based on Sidhant Aggarwal's boolean query parser.
 * https://github.com/sidhant92/boolparser
 */
public class SidhantAggarwalBooleanQueryParser implements BooleanQueryParser {

    private Datastore datastore;

    private Set<BooleanQueryOperator> operators;

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

        final var operator = operators.stream()
                .filter(o -> o.matches(base, node.getField()))
                .findFirst()
                .orElse(DEFAULT);

        final var evaluation = operator.evaluate(base, node.getField());
        final var field = evaluation.getField();
        final var valueOpt = evaluation.getValue(node.getValue());

        if (valueOpt.isEmpty()) {
            consumer.filter(eq("_id", null));
            return base;
        }

        final Object v = valueOpt.get();

        if (v instanceof Filter f) {
            consumer.filter(f);                      // direct filter (regex/elemMatch/etc.)
        } else if (v instanceof java.util.Collection<?> c) {
            consumer.filter(in(field, c));           // multi-match via $in
        } else {
            consumer.filter(eq(field, v));           // single match
        }

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

    public Set<BooleanQueryOperator> getOperators() {
        return operators;
    }

    @Inject
    public void setOperators(Set<BooleanQueryOperator> operators) {
        this.operators = operators;
    }

}
