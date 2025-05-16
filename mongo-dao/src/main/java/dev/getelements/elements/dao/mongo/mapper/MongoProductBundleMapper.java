package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoProductBundle;
import dev.getelements.elements.sdk.model.application.ProductBundle;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = MongoProductBundleRewardMapper.class)
public interface MongoProductBundleMapper extends MapperRegistry.ReversibleMapper<MongoProductBundle, ProductBundle> {

    @Override
    ProductBundle forward(MongoProductBundle source);

    @Override
    MongoProductBundle reverse(ProductBundle source);

}
