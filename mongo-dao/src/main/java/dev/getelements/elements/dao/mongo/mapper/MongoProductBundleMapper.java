package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductBundle;
import dev.getelements.elements.dao.mongo.model.goods.MongoProductBundleReward;
import dev.getelements.elements.sdk.model.application.Application;
import dev.getelements.elements.sdk.model.application.ProductBundleReward;
import dev.getelements.elements.sdk.model.goods.ProductBundle;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoProductBundleMapper extends MapperRegistry.ReversibleMapper<MongoProductBundle, ProductBundle> {

    @Override
    @Mapping(target = "id", source = "objectId")
    @Mapping(target = "tags", source = "tags")
    ProductBundle forward(MongoProductBundle source);

    @Override
    @InheritInverseConfiguration
    @Mapping(target = "tags", source = "tags")
    MongoProductBundle reverse(ProductBundle source);

    ProductBundleReward forward(MongoProductBundleReward source);

    MongoProductBundleReward reverse(ProductBundleReward source);

}
