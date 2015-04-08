package com.namazustudios.promotion.dao.mongo;

import com.google.common.base.Strings;
import com.namazustudios.promotion.Constants;
import com.namazustudios.promotion.dao.mongo.model.MongoShortLink;
import org.mongodb.morphia.Datastore;

import javax.inject.Inject;
import javax.inject.Named;
import javax.inject.Singleton;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoShortLinkDao {

    @Inject
    private Datastore datastore;

    @Inject
    @Named(Constants.SHORT_LINK_BASE)
    private String shortLinkBase;

    public MongoShortLink createShortLinkFromURL(final String url) {

        if (url == null) {
            throw new IllegalArgumentException("URL must not be null.");
        }

        final MongoShortLink mongoShortLink = new MongoShortLink();
        mongoShortLink.setDestinationUrl(Strings.nullToEmpty(url).trim());
        datastore.save(mongoShortLink);
        return mongoShortLink;

    }

    public String getURLFromShortLink(MongoShortLink mongoShortLink) {
        final String objectId = mongoShortLink.getObjectId();
        return shortLinkBase + objectId;
    }

}
