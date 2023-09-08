package dev.getelements.elements.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.io.BaseEncoding;
import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.ShortLinkDao;
import dev.getelements.elements.dao.mongo.model.MongoShortLink;
import dev.getelements.elements.exception.InvalidDataException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.ShortLink;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;

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
        return Pagination.empty();
    }

    @Override
    public ShortLink getShortLinkWithId(String id) {

        final ObjectId objectId = mongoDBUtils.parseOrThrowNotFoundException(id);
        final MongoShortLink mongoShortLink = datastore.find(MongoShortLink.class)
                .filter(Filters.eq("_id", objectId)).first();

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

        final MongoShortLink mongoShortLink = datastore.find(MongoShortLink.class)
                .filter(Filters.eq("_id", objectId)).first();

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

        final DeleteResult deleteResult = datastore.find(MongoShortLink.class)
                .filter(Filters.eq("_id", objectId)).delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException();
        }

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

                }, new FindOptions());
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
