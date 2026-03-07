package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.iap.MongoIapSku;
import dev.getelements.elements.dao.mongo.model.iap.MongoIapSkuReward;
import dev.getelements.elements.sdk.model.iap.IapSku;
import dev.getelements.elements.sdk.model.iap.IapSkuReward;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class})
public interface MongoIapSkuMapper extends MapperRegistry.ReversibleMapper<MongoIapSku, IapSku> {

    @Override
    @Mapping(target = "id", source = "objectId")
    IapSku forward(MongoIapSku source);

    @Override
    @InheritInverseConfiguration
    MongoIapSku reverse(IapSku source);

    IapSkuReward forward(MongoIapSkuReward source);

    MongoIapSkuReward reverse(IapSkuReward source);

}
