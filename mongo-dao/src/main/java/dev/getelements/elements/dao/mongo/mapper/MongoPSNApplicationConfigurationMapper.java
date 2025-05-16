package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.application.MongoPSNApplicationConfiguration;
import dev.getelements.elements.sdk.model.application.PSNApplicationConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.InheritInverseConfiguration;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(uses = {PropertyConverters.class, MongoApplicationMapper.class})
public interface MongoPSNApplicationConfigurationMapper extends MapperRegistry.ReversibleMapper<
        MongoPSNApplicationConfiguration,
        PSNApplicationConfiguration> {

    @Override
    @Mapping(target = "id", source = "objectId")
    PSNApplicationConfiguration forward(MongoPSNApplicationConfiguration source);

    @Override
    @InheritInverseConfiguration
    MongoPSNApplicationConfiguration reverse(PSNApplicationConfiguration source);

}
