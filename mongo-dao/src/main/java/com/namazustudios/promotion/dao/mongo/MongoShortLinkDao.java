package com.namazustudios.promotion.dao.mongo;

import com.namazustudios.promotion.dao.mongo.model.MongoShortLink;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;
import java.net.URI;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoShortLinkDao {

    @Inject
    private Datastore datastore;

    @Inject
    @Named("com.namazustudios.promotion.short.link.base")
    private String shortLinkBase;

    public MongoShortLink createShortLinkFromURL(final String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        final MongoShortLink mongoShortLink = new MongoShortLink();
        mongoShortLink.setDestinationUrl(url.trim());
        datastore.save(mongoShortLink);
        return mongoShortLink;

    }

    public String getURLFromShortLink(MongoShortLink mongoShortLink) {
        final String objectId = mongoShortLink.getObjectId();
        return shortLinkBase + objectId;
    }

}
