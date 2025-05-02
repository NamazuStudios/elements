package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoProductBundleReward;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoProductBundleRewardMapper extends MapperRegistry.ReversibleMapper<
        MongoProductBundleReward,
        ProductBundleReward> {

    @Override
    ProductBundleReward forward(MongoProductBundleReward source);

    @Override
    MongoProductBundleReward reverse(ProductBundleReward source);

}
