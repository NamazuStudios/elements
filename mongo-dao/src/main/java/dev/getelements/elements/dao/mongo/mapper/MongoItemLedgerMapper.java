package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.goods.MongoItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoItemLedgerMapper
        extends MapperRegistry.ReversibleMapper<MongoItemLedgerEntry, ItemLedgerEntry> {

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "timestamp", source = "timestamp")
    ItemLedgerEntry forward(MongoItemLedgerEntry source);

    @Override
    @Mapping(target = "id", source = "id")
    @Mapping(target = "timestamp", source = "timestamp")
    MongoItemLedgerEntry reverse(ItemLedgerEntry source);
}
