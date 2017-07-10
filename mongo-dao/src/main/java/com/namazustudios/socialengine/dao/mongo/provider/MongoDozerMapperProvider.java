package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.dao.mongo.converter.ObjectIdConverter;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.*;
import com.namazustudios.socialengine.model.profile.Profile;
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

            mapping(User.class, MongoUser.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(ApplicationConfiguration.class, MongoApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("parent.id", "parent.objectId", customConverter(ObjectIdConverter.class));

            mapping(PSNApplicationConfiguration.class, MongoPSNApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("npIdentifier", "uniqueIdentifier");

            mapping(IosApplicationConfiguration.class, MongoIosApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(GooglePlayApplicationConfiguration.class, MongoGooglePlayApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(FacebookApplicationConfiguration.class, MongoFacebookApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("applicationId","uniqueIdentifier");

            mapping(Profile.class, MongoProfile.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("application.id", "application.objectId", customConverter(ObjectIdConverter.class));

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
