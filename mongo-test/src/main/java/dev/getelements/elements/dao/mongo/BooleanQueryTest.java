package dev.getelements.elements.dao.mongo;

import com.github.sidhant92.boolparser.parser.canopy.PEGBoolExpressionParser;

public class BooleanQueryTest {

    public static void main(String [] args) throws Exception {

        final var console = System.console();
        final var parser = new PEGBoolExpressionParser();

        do {

            final var query = console.readLine("Query: ");
            final var parsed = parser.parseExpression(query);

            if (parsed.isEmpty()) {
                System.out.println("Invalid query.");
                continue;
            }

    

        } while (true);

    }
}
