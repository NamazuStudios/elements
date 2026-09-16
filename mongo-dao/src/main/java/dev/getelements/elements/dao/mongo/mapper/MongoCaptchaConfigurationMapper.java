package dev.getelements.elements.dao.mongo.mapper;

import dev.getelements.elements.dao.mongo.model.auth.MongoCaptchaConfiguration;
import dev.getelements.elements.sdk.model.auth.CaptchaConfiguration;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.mapstruct.Mapper;

@Mapper(uses = {PropertyConverters.class})
public interface MongoCaptchaConfigurationMapper
        extends MapperRegistry.ReversibleMapper<MongoCaptchaConfiguration, CaptchaConfiguration> {

    @Override
    CaptchaConfiguration forward(MongoCaptchaConfiguration source);

    @Override
    MongoCaptchaConfiguration reverse(CaptchaConfiguration source);

}
