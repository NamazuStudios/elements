package dev.getelements.elements.dao.mongo.goods;

import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.goods.MongoItemLedgerEntry;
import dev.getelements.elements.sdk.dao.ItemLedgerDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;

import java.util.Date;

import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.filters.Filters.eq;

public class MongoItemLedgerDao implements ItemLedgerDao {

    private Datastore datastore;

    private MongoDBUtils mongoDBUtils;

    private MapperRegistry mapperRegistry;

    @Override
    public ItemLedgerEntry createLedgerEntry(final ItemLedgerEntry entry) {
        final var mongo = getMapperRegistry().map(entry, MongoItemLedgerEntry.class);
        mongo.setTimestamp(new Date());
        getDatastore().insert(mongo);
        return getMapperRegistry().map(mongo, ItemLedgerEntry.class);
    }

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntries(
            final String inventoryItemId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        final var query = getDatastore().find(MongoItemLedgerEntry.class)
                .filter(eq("inventoryItemId", inventoryItemId));
        applyEventTypeFilter(query, eventType);
        final var options = new FindOptions().sort(descending("timestamp"));
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                m -> getMapperRegistry().map(m, ItemLedgerEntry.class),
                options);
    }

    @Override
    public Pagination<ItemLedgerEntry> getLedgerEntriesForUser(
            final String userId, final int offset, final int count,
            final ItemLedgerEventType eventType) {
        final var query = getDatastore().find(MongoItemLedgerEntry.class)
                .filter(eq("userId", userId));
        applyEventTypeFilter(query, eventType);
        final var options = new FindOptions().sort(descending("timestamp"));
        return getMongoDBUtils().paginationFromQuery(
                query, offset, count,
                m -> getMapperRegistry().map(m, ItemLedgerEntry.class),
                options);
    }

    private void applyEventTypeFilter(final Query<MongoItemLedgerEntry> query,
                                      final ItemLedgerEventType eventType) {
        if (eventType != null) {
            query.filter(eq("eventType", eventType));
        }
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(final Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(final MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(final MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }
}
