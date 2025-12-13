package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.receipt.MongoReceipt;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoReceiptMapper extends MapperRegistry.ReversibleMapper<MongoReceipt, Receipt> {

    @Override
    @Mapping(
            target = "body",
            expression = "java(source.getBody() == null ? null : source.getBody().toJson())"
    )
    @Mapping(target = "user.id", source = "user.objectId")
    Receipt forward(MongoReceipt source);

    @Override
    @Mapping(
            target = "body",
            expression = "java(source.getBody() == null ? null : org.bson.Document.parse(source.getBody()))"
    )
    @Mapping(target = "user.objectId", source = "user.id")
    MongoReceipt reverse(Receipt source);

}

