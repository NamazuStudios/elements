package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.mongodb.WriteResult;
import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.ShortLinkDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoShortLink;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.net.URI;
import java.net.URISyntaxException;

/**
 * Created by patricktwohig on 3/26/15.
 */
@Singleton
public class MongoShortLinkDao implements ShortLinkDao {

    @Inject
    private Datastore datastore;

    @Inject
    private ObjectIndex objectIndex;

    @Inject
    private MongoDBUtils mongoDBUtils;

    @Inject
    private ValidationHelper validationHelper;

    @Inject
    private StandardQueryParser standardQueryParser;

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count) {
        final Query<MongoShortLink> query = datastore.find(MongoShortLink.class);
        return paginationFromQuery(query, offset, count);
    }

    @Override
    public Pagination<ShortLink> getShortLinks(int offset, int count, String queryString) {

        final org.apache.lucene.search.Query searchQuery;

        try {
            searchQuery = standardQueryParser.parse(queryString, "name");
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return mongoDBUtils.paginationFromSearch(
                MongoShortLink.class, searchQuery,
                offset, count,
                (Function<MongoShortLink, ShortLink>) input -> transform(input));

    }

    @Override
    public ShortLink getShortLinkWithId(String id) {

        final ObjectId objectId = mongoDBUtils.parse(id);
        final MongoShortLink mongoShortLink = datastore.get(MongoShortLink.class, objectId);

        if (mongoShortLink == null) {
            throw new NotFoundException("short link with id " + id + " not found");
        }

        return transform(mongoShortLink);

    }

    @Override
    public ShortLink getShortLinkWithPath(String shortLinkPath) {

        final String shortLinkPaths[] = shortLinkPath.split("[/]+");

        if (shortLinkPaths.length != 1) {
            throw new NotFoundException();
        }

        final ObjectId objectId;

        try {
            final byte[] bytes = BaseEncoding.base64Url().decode(shortLinkPaths[0]);
            objectId = new ObjectId(bytes);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException();
        }

        final MongoShortLink mongoShortLink = datastore.get(MongoShortLink.class, objectId);

        if (mongoShortLink == null) {
            throw new NotFoundException();
        }

        return transform(mongoShortLink);

    }

    @Override
    public ShortLink create(ShortLink link) {

        validate(link);

        final MongoShortLink mongoShortLink = new MongoShortLink();
        mongoShortLink.setDestinationUrl(link.getDestinationURL());

        datastore.save(mongoShortLink);
        objectIndex.index(mongoShortLink);

        return transform(mongoShortLink);

    }

    @Override
    public ShortLink createShortLinkFromURL(final String url) {
        final ShortLink shortLink = new ShortLink();
        shortLink.setDestinationURL(url);
        return create(shortLink);
    }

    @Override
    public void deleteShortLink(String id) {

        final ObjectId objectId;

        try {
            objectId = new ObjectId(id);
        } catch (IllegalArgumentException ex) {
            throw new NotFoundException();
        }

        final WriteResult writeResult = datastore.delete(MongoShortLink.class, objectId);

        if (writeResult.getN() == 0) {
            throw new NotFoundException();
        }

        objectIndex.delete(MongoShortLink.class, id);

    }

    public MongoShortLink createMongoShortLinkFromURL(final String url) {

        if (url == null) {
            throw new InvalidDataException("URL must not be null.");
        }

        final MongoShortLink mongoShortLink = new MongoShortLink();
        mongoShortLink.setDestinationUrl(Strings.nullToEmpty(url).trim());

        datastore.save(mongoShortLink);

        return mongoShortLink;

    }

    public ShortLink transform(final MongoShortLink mongoShortLink) {
        final ShortLink shortLink = new ShortLink();
        final ObjectId objectId = mongoShortLink.getObjectId();

        if (objectId != null) {
            shortLink.setId(mongoShortLink.getObjectId().toHexString());
        }

        shortLink.setShortLinkPath(BaseEncoding.base64Url().encode(objectId.toByteArray()));
        shortLink.setDestinationURL(mongoShortLink.getDestinationUrl());

        return shortLink;
    }

    private Pagination<ShortLink> paginationFromQuery(final Query<MongoShortLink> mongoShortLinkQuery,
                                                      int offset, int count) {
        return mongoDBUtils.paginationFromQuery(mongoShortLinkQuery, offset, count,
                new Function<MongoShortLink, ShortLink>() {

                    @Override
                    public ShortLink apply(MongoShortLink input) {
                        return transform(input);
                    }

                });
    }

    public void validate(final ShortLink shortLink) {

        if (shortLink == null) {
            throw new InvalidDataException("ShortLink must not be null.");
        }

        validationHelper.validateModel(shortLink);
        shortLink.setDestinationURL(shortLink.getDestinationURL().trim());

        try {
            new URI(shortLink.getDestinationURL());
        } catch (URISyntaxException ex) {
            throw new InvalidDataException(shortLink.getDestinationURL() + " is not a valid URI");
        }

    }

}
