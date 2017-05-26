package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.dao.mongo.converter.ObjectIdConverter;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationProfile;
import com.namazustudios.socialengine.model.application.PSNApplicationProfile;
import org.dozer.DozerBeanMapper;
import org.dozer.Mapper;
import org.dozer.loader.api.BeanMappingBuilder;

import javax.inject.Provider;

import static org.dozer.loader.api.FieldsMappingOptions.customConverter;

/**
 * Created by patricktwohig on 5/25/17.
 */
public class MongoDozerMapperProvider implements Provider<Mapper> {

    @Override
    public Mapper get() {

        final BeanMappingBuilder beanMappingBuilder = new BeanMappingBuilder() {
            @Override
            protected void configure() {
                mapping(PSNApplicationProfile.class, MongoPSNApplicationProfile.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class));
            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
