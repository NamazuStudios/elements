package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.dao.mongo.converter.ObjectIdConverter;
import com.namazustudios.socialengine.dao.mongo.model.MongoGooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.MongoIosApplicationConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.MongoPSNApplicationConfiguration;
import com.namazustudios.socialengine.model.application.GooglePlayApplicationConfiguration;
import com.namazustudios.socialengine.model.application.IosApplicationConfiguration;
import com.namazustudios.socialengine.model.application.PSNApplicationConfiguration;
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

                mapping(PSNApplicationConfiguration.class, MongoPSNApplicationConfiguration.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("npIdentifier", "name");

                mapping(IosApplicationConfiguration.class, MongoIosApplicationConfiguration.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("applicationId","name");

                mapping(GooglePlayApplicationConfiguration.class, MongoGooglePlayApplicationConfiguration.class)
                    .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                    .fields("applicationId","name");

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
