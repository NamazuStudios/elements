package dev.getelements.elements.dao.mongo.provider;

import dev.getelements.elements.common.util.mapstruct.MapstructMapperRegistryBuilder;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import jakarta.inject.Provider;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDozerMapperProvider implements Provider<MapperRegistry> {

    @Override
    public MapperRegistry get() {
        return new MapstructMapperRegistryBuilder()
                .withPackages("dev.getelements.elements.dao.mongo.mapper")
                .build();
    }

}
