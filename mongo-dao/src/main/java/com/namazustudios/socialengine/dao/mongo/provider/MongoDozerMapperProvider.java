package com.namazustudios.socialengine.dao.mongo.provider;

import com.namazustudios.socialengine.dao.mongo.converter.ObjectIdConverter;
import com.namazustudios.socialengine.dao.mongo.model.*;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.application.*;
import com.namazustudios.socialengine.model.match.Match;
import com.namazustudios.socialengine.model.match.MatchTimeDelta;
import com.namazustudios.socialengine.model.notification.FCMRegistration;
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
                .fields("parent.id", "parent.objectId", customConverter(ObjectIdConverter.class))
                .fields("uniqueIdentifier", "uniqueIdentifier");

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

            mapping(MatchmakingApplicationConfiguration.class, MongoMatchmakingApplicationConfiguration.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("scheme", "uniqueIdentifier");

            mapping(Profile.class, MongoProfile.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class))
                .fields("application.id", "application.objectId", customConverter(ObjectIdConverter.class));

            mapping(Match.class, MongoMatch.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(MatchTimeDelta.class, MongoMatchDelta.class)
                .fields("id", "key.match", customConverter(ObjectIdConverter.class))
                .fields("snapshot.id", "key.match", customConverter(ObjectIdConverter.class))
                .fields("timeStamp", "key.timeStamp");

            mapping(MongoMatch.class, MongoMatchSnapshot.class)
                .fields("player.objectId", "player.objectId", customConverter(ObjectIdConverter.class))
                .fields("opponent.objectId", "opponent.objectId", customConverter(ObjectIdConverter.class));

            mapping(Application.class, MongoApplication.class)
                .fields("id", "objectId", customConverter(ObjectIdConverter.class));

            mapping(FCMRegistration.class, MongoFCMRegistration.class)
                .fields("id", "objectId")
                .fields("profile.id", "profile.objectId");

            }
        };

        final DozerBeanMapper dozerBeanMapper = new DozerBeanMapper();
        dozerBeanMapper.addMapping(beanMappingBuilder);
        return dozerBeanMapper;

    }

}
