package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.dao.Indexable;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexOperation;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.InternalException;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.Set;
import java.util.concurrent.*;

import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.System.currentTimeMillis;
import static java.util.UUID.randomUUID;
import static java.util.concurrent.TimeUnit.MILLISECONDS;
import static java.util.concurrent.TimeUnit.SECONDS;

public class MongoIndexer implements IndexDao.Indexer {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexer.class);

    private static final String GLOBAL_OPERATION_ID = "global";

    private static final long TIMEOUT = MILLISECONDS.convert(60, SECONDS);

    private static final long HEARTBEAT_INTERVAL_SECONDS = 30;

    private final Datastore datastore;

    private final MongoDBUtils mongoDBUtils;
    private final Set<Indexable> indexableSet;

    private final ScheduledFuture<?> heartbeatTask;

    private final MongoIndexOperation indexOperation;

    private static final ScheduledExecutorService heartbeat = Executors.newScheduledThreadPool(0);

    @Inject
    public MongoIndexer(
            final Datastore datastore,
            final MongoDBUtils mongoDBUtils,
            final Set<Indexable> indexableSet) {

        this.datastore = datastore;
        this.mongoDBUtils = mongoDBUtils;
        this.indexableSet = indexableSet;

        final var now = new Timestamp(currentTimeMillis());
        final var uuid = randomUUID().toString();

        final var query = datastore.find(MongoIndexOperation.class).filter(
                eq("_id", GLOBAL_OPERATION_ID),
                lt("expiry", now),
                eq("uuid", uuid)
        );

        final var expiry = nextTimeout();
        final var options = new ModifyOptions()
                .upsert(true)
                .returnDocument(AFTER);

        indexOperation = mongoDBUtils.perform(
            ds -> query.modify(options,
                    set("_id", GLOBAL_OPERATION_ID),
                    set("expiry", expiry),
                    set("uuid", uuid)
            ),
            ex -> new DuplicateException("Indexing already in progress.", ex)
        );

        heartbeatTask = heartbeat.scheduleAtFixedRate(
                this::refresh,
                HEARTBEAT_INTERVAL_SECONDS,
                HEARTBEAT_INTERVAL_SECONDS,
                SECONDS
        );

    }

    private Timestamp nextTimeout() {
        return new Timestamp(currentTimeMillis() + TIMEOUT);
    }

    private void refresh() {

        final var now = new Timestamp(currentTimeMillis());
        final var uuid = indexOperation.getUuid();

        final var query = datastore.find(MongoIndexOperation.class).filter(
                eq("_id", GLOBAL_OPERATION_ID),
                gt("expiry", now),
                eq("uuid", uuid)
        );

        final var expiry = nextTimeout();
        final var options = new ModifyOptions()
                .upsert(true)
                .returnDocument(AFTER);

        mongoDBUtils.perform(
                ds -> query.modify(options,
                        set("_id", GLOBAL_OPERATION_ID),
                        set("expiry", expiry),
                        set("uuid", uuid)
                ),
                ex -> new DuplicateException("Indexing already in progress.", ex)
        );

    }

    @Override
    public void buildAllCustom() {
        indexableSet.forEach(Indexable::buildIndexes);
    }

    @Override
    public void close() {

        datastore.delete(indexOperation);
        heartbeatTask.cancel(false);

        try {
            heartbeatTask.get();
        } catch (InterruptedException ex) {
            throw new InternalException("Interrupted clearing index operation", ex);
        } catch (ExecutionException ex) {
            throw new RuntimeException("Index operation failed", ex.getCause());
        } catch (CancellationException ex) {
            logger.debug("Got cancellation (expected).", ex);
        }

    }

}
