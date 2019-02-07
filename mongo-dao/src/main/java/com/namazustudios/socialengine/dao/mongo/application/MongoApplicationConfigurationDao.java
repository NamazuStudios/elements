package com.namazustudios.socialengine.dao.mongo.application;

import com.namazustudios.socialengine.dao.ApplicationConfigurationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplicationConfiguration;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.application.ApplicationConfiguration;
import com.namazustudios.socialengine.model.application.ConfigurationCategory;
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
import java.util.List;
import java.util.stream.Collectors;

/**
 * Created by patricktwohig on 7/13/15.
 */
public class MongoApplicationConfigurationDao implements ApplicationConfigurationDao {

    private Mapper beanMapper;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoApplicationDao mongoApplicationDao;

    @Override
    public <T extends ApplicationConfiguration> List<T> getApplicationConfigurationsForApplication(
            String applicationNameOrId,
            ConfigurationCategory configurationCategory,
            Class<T> type) {
        final MongoApplication parent = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfiguration> query =
                getDatastore().createQuery(MongoApplicationConfiguration.class);
        query.field("parent").equal(parent);
        query.field("category").equal(configurationCategory);

        List<T> applicationConfigurations = query
            .asList().stream()
            .map(mac -> getBeanMapper().map(mac, type))
            .collect(Collectors.toList());

        return applicationConfigurations;
    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                                   final int offset, final int count) {

        final MongoApplication mongoApplication;
        mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationNameOrId);

        final Query<MongoApplicationConfiguration> query;
        query = getDatastore().createQuery(MongoApplicationConfiguration.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("parent").equal(mongoApplication)
        );

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> getBeanMapper().map(input, ApplicationConfiguration.class));

    }

    @Override
    public Pagination<ApplicationConfiguration> getActiveApplicationConfigurations(final String applicationNameOrId,
                                                                                   final int offset, final int count,
                                                                                   final String search) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {

            final Term activeTerm = new Term("active", "true");
            final Term applicationIdTerm = new Term("applicationId");
            final Term applicationNameTerm = new Term("applicationName");

            booleanQueryBuilder.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(new TermQuery(applicationIdTerm), BooleanClause.Occur.SHOULD);
            booleanQueryBuilder.add(new TermQuery(applicationNameTerm), BooleanClause.Occur.SHOULD);
            booleanQueryBuilder.add(getStandardQueryParser().parse(search, "name"), BooleanClause.Occur.FILTER);

        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoApplicationConfiguration.class, booleanQueryBuilder.build(), offset, count, input -> getBeanMapper().map(input, ApplicationConfiguration.class));

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
