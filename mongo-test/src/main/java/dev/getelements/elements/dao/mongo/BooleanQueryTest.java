package dev.getelements.elements.dao.mongo;

import com.github.sidhant92.boolparser.domain.*;
import com.github.sidhant92.boolparser.parser.canopy.PEGBoolExpressionParser;

import java.util.Arrays;
import java.util.Scanner;

public class BooleanQueryTest {

    public static void main(String [] args) {

        final var scanner = new Scanner(System.in);
        final var parser = new PEGBoolExpressionParser();

        do {

            System.out.print("Query $> ");
            final var query = scanner.nextLine();
            final var parsed = parser.parseExpression(query);

            if (parsed.isEmpty()) {
                System.out.println("Invalid query.");
            } else {
                print(0, parsed.get());
            }

        } while (true);

    }


    private static void print(final int indent, final Node node) {
        switch (node.getNodeType()) {
            case STRING_TOKEN:
                print(indent, (StringToken)node);
                break;
            case NUMERIC_TOKEN:
                print(indent, (NumericToken)node);
                break;
            case BOOL_EXPRESSION:
                print(indent, (BoolExpression)node);
                break;
            case NUMERIC_RANGE_TOKEN:
                print(indent, (NumericRangeToken)node);
                break;
            default:
                throw new UnsupportedOperationException("Unexpected token type: " + node.getNodeType());
        }
    }

    private static String prefix(final int indent) {
        final var array = new char[indent + 1];
        Arrays.fill(array, 0, array.length, '-');
        return String.valueOf(array) + "> ";
    }

    private static void printf(int indent, final String format, final Object ... args) {
        System.out.printf(prefix(indent) + format, args);
    }

    private static void print(final int indent, final StringToken node) {
        printf(indent, "String Token %s = %s\n", node.getField(), node.getValue());
    }

    private static void print(final int indent, final NumericToken node) {
        printf(indent,"Numeric Token %s %s = %s %s %s\n",
                prefix(indent),
                node.getDataType(),
                node.getField(),
                node.getDataType(),
                node.getOperator(),
                node.getValue()
        );
    }

    private static void print(final int indent, final BoolExpression node) {
        if (!node.getOrOperations().isEmpty()) {
            printf(indent, "OR\n");
            node.getOrOperations().forEach(n -> print(indent + 1, n));
        }

        if (!node.getAndOperations().isEmpty()) {
            printf(indent, "AND\n");
            node.getAndOperations().forEach(n -> print(indent + 1, n));
        }

        if (!node.getNotOperations().isEmpty()) {
            printf(indent, "NOT\n");
            node.getNotOperations().forEach(n -> print(indent + 1, n));
        }
    }

    private static void print(final int indent, final NumericRangeToken node) {
        printf(indent,"Range %s [%s - %s IN %s - %s - %s]\n",
                node.getField(),
                node.getFromValue(),
                node.getFromDataType(),
                node.getToValue(),
                node.getToDataType()
        );
    }

}