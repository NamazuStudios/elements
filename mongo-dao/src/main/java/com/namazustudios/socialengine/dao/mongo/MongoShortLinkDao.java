package com.namazustudios.socialengine.dao.mongo;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.namazustudios.socialengine.dao.ShortLinkDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoShortLink;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ShortLink;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.mongodb.morphia.Datastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;
import javax.inject.Singleton;

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
                new Function<MongoShortLink, ShortLink>() {
                    @Override
                    public ShortLink apply(MongoShortLink input) {
                        return transform(input);
                    }
                });
    }

    @Override
    public ShortLink getShortLinkWithId(String id) {
        return null;
    }

    @Override
    public ShortLink getShortLinkWithPath(String shortLinkPath) {
        return null;
    }

    @Override
    public ShortLink create(ShortLink link) {
        return null;
    }

    @Override
    public ShortLink createShortLinkFromURL(final String url) {
        return null;
    }

    @Override
    public void deleteShortLink(String id) {

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
        return null;
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

}
