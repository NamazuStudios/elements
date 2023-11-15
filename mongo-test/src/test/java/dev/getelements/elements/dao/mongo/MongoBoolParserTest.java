package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoScore;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

@Guice(modules = IntegrationTestModule.class)
public class MongoBoolParserTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoBoolParserTest.class);

    private BooleanQueryParser booleanQueryParser;

    @Test
    public void testSimpleQuery() {
        final var query = getBooleanQueryParser().parse(MongoItem.class, "name:test").get();
        final var explanation = query.explain();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testNumericQuery() {
        final var query = getBooleanQueryParser().parse(MongoScore.class, "pointValue > 42").get();
        final var explanation = query.explain();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testNumericRangeQuery() {
        final var query = getBooleanQueryParser().parse(MongoScore.class, "pointValue:0 TO 42").get();
        final var explanation = query.explain();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testBooleanQuery() {
        final var query = getBooleanQueryParser().parse(MongoItem.class, "name:a OR name:b").get();
        final var explanation = query.explain();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testBooleanQueryNested() {

        final var query = getBooleanQueryParser().parse(MongoScore.class,
                "(pointValue > 0 AND pointValue < 100) OR (pointValue > 200 AND pointValue< 300)"
        ).get();

        final var explanation = query.explain();
        logger.info("Query {} - {}", query, explanation);

    }


    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

}
