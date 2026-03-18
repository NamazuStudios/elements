package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.goods.MongoProductSkuSchema;
import dev.getelements.elements.sdk.model.goods.ProductSkuSchema;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoProductSkuSchemaMapper extends MapperRegistry.ReversibleMapper<MongoProductSkuSchema, ProductSkuSchema> {

    @Override
    @Mapping(target = "id", source = "objectId")
    ProductSkuSchema forward(MongoProductSkuSchema source);

    @Override
    @InheritInverseConfiguration
    MongoProductSkuSchema reverse(ProductSkuSchema source);

}
