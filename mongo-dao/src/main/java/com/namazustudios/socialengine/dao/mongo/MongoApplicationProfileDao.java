package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ApplicationProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.AbstractMongoApplicationProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationProfile;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationProfileDao implements ApplicationProfileDao {

    private Mapper beanMapper;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<AbstractMongoApplicationProfile> query;
        query = getDatastore().createQuery(AbstractMongoApplicationProfile.class);

        query.filter("active =", true);
        query.filter("parent =", mongoApplication);

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getBeanMapper().map(input, ApplicationProfile.class));

    }

    @Override
    public Pagination<ApplicationProfile> getActiveApplicationProfiles(final String applicationNameOrId,
                                                                       final int offset, final int count,
                                                                       final String search) {

        final BooleanQuery booleanQuery = new BooleanQuery();

        try {

            final Term activeTerm = new Term("active", "true");
            final Term applicationIdTerm = new Term("applicationId");
            final Term applicationNameTerm = new Term("applicationName");

            booleanQuery.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQuery.add(new TermQuery(applicationIdTerm), BooleanClause.Occur.SHOULD);
            booleanQuery.add(new TermQuery(applicationNameTerm), BooleanClause.Occur.SHOULD);
            booleanQuery.add(getStandardQueryParser().parse(search, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(AbstractMongoApplicationProfile.class, booleanQuery, offset, count, input -> getBeanMapper().map(input, ApplicationProfile.class));

    }

    public StandardQueryParser getStandardQueryParser() {
        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

}
