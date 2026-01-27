package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.mongo.model.score.MongoScore;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.dao.UserDao;
import jakarta.inject.Provider;
import org.bson.types.ObjectId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

@Guice(modules = IntegrationTestModule.class)
public class MongoBoolParserTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoBoolParserTest.class);

    private BooleanQueryParser booleanQueryParser;

    private Provider<Transaction> transactionProvider;

    @Test
    public void testSimpleQuery() {
        final var query = getBooleanQueryParser().parse(MongoItem.class, "name:test").get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testNumericQuery() {
        final var query = getBooleanQueryParser().parse(MongoScore.class, "pointValue > 42").get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testNumericRangeQuery() {
        final var query = getBooleanQueryParser().parse(MongoScore.class, "pointValue:0 TO 42").get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testBooleanQuery() {
        final var query = getBooleanQueryParser().parse(MongoItem.class, "name:a OR name:b").get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testBooleanQueryNested() {

        final var query = getBooleanQueryParser().parse(MongoScore.class,
                "(pointValue > 0 AND pointValue < 100) OR (pointValue > 200 AND pointValue< 300)"
        ).get();

        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);

    }

    @Test
    public void testReferenceQuery() {
        final var formatted = String.format(".ref.profile:%s", new ObjectId());
        final var query = getBooleanQueryParser().parse(MongoScore.class, formatted).get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testNameQuery() {
        final var query = getBooleanQueryParser().parse(MongoDistinctInventoryItem.class, "item.name:test").get();
        final var explanation = query.explain();
        query.first();
        logger.info("Query {} - {}", query, explanation);
    }

    @Test
    public void testWorksInTransactions() {
        getTransactionProvider().get().performAndCloseV(txn -> {
            final var dao = txn.getDao(ProfileDao.class);
            dao.getActiveProfiles(0, 100, "displayName:test");
        });
    }

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

}
