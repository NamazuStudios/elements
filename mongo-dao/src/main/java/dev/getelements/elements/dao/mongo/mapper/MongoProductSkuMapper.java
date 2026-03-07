package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.goods.MongoProductSku;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductSkuReward;
import dev.getelements.elements.sdk.model.goods.ProductSku;
import dev.getelements.elements.sdk.model.goods.ProductSkuReward;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoProductSkuMapper extends MapperRegistry.ReversibleMapper<MongoProductSku, ProductSku> {

    @Override
    @Mapping(target = "id", source = "objectId")
    ProductSku forward(MongoProductSku source);

    @Override
    @InheritInverseConfiguration
    MongoProductSku reverse(ProductSku source);

    ProductSkuReward forward(MongoProductSkuReward source);

    MongoProductSkuReward reverse(ProductSkuReward source);

}
