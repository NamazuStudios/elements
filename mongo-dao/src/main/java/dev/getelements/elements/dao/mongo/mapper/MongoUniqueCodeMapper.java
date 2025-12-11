package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.ucode.MongoUniqueCode;
import dev.getelements.elements.sdk.model.ucode.UniqueCode;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class, MongoDBMapper.class} )
public interface MongoUniqueCodeMapper extends MapperRegistry.ReversibleMapper<MongoUniqueCode, UniqueCode> {

    @Override
    MongoUniqueCode reverse(UniqueCode source);

    @Override
    UniqueCode forward(MongoUniqueCode source);

}
