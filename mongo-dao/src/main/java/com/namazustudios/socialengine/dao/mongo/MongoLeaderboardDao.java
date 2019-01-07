package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoCommandException;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.LeaderboardDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoLeaderboard;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.leaderboard.Leaderboard;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

public class MongoLeaderboardDao implements LeaderboardDao {

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private Mapper beanMapper;

    @Override
    public Leaderboard createLeaderboard(final Leaderboard leaderboard) {

        getValidationHelper().validateModel(leaderboard, ValidationGroups.Create.class);

        final MongoLeaderboard mongoLeaderboard = getBeanMapper().map(leaderboard, MongoLeaderboard.class);

        try {
            getDatastore().insert(mongoLeaderboard);
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException(ex);
        }

        objectIndex.index(mongoLeaderboard);
        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count) {
        final Query<MongoLeaderboard> query = datastore.createQuery(MongoLeaderboard.class);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, l -> getBeanMapper().map(l, Leaderboard.class));
    }

    @Override
    public Pagination<Leaderboard> getLeaderboards(final int offset, final int count, final String search) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {
            booleanQueryBuilder.add(standardQueryParser.parse(search, "name"), BooleanClause.Occur.FILTER);
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return mongoDBUtils.paginationFromSearch(
            MongoLeaderboard.class,
            booleanQueryBuilder.build(),
            offset, count,
            l -> getBeanMapper().map(l, Leaderboard.class));

    }

    @Override
    public Leaderboard getLeaderboard(final String nameOrId) {
        final MongoLeaderboard mongoLeaderboard = getMongoLeaderboard(nameOrId);
        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    public MongoLeaderboard getMongoLeaderboard(final String nameOrId) {

        final Query<MongoLeaderboard> query = datastore.createQuery(MongoLeaderboard.class);

        try {
            query.filter("_id", new ObjectId(nameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", nameOrId);
        }

        final MongoLeaderboard mongoLeaderboard = query.get();

        if (mongoLeaderboard == null) {
            throw new LeaderboardNotFoundException("Leaderboard " + nameOrId + " not found.");
        }

        return mongoLeaderboard;

    }

    @Override
    public Leaderboard updateLeaderboard(final String leaderboardNameOrId, final Leaderboard leaderboard) {

        if (leaderboardNameOrId == null) {
            throw new IllegalArgumentException("Leaderboard name must not be null");
        }

        getValidationHelper().validateModel(leaderboard, ValidationGroups.Update.class);

        final Query<MongoLeaderboard> query = datastore.createQuery(MongoLeaderboard.class);

        try {
            query.field("_id").equal(new ObjectId(leaderboardNameOrId));
        } catch (IllegalArgumentException ex) {
            query.field("name").equal(leaderboardNameOrId);
        }

        final UpdateOperations<MongoLeaderboard> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoLeaderboard.class);

        updateOperations.set("name", leaderboard.getName());
        updateOperations.set("title", leaderboard.getTitle());
        updateOperations.set("scoreUnits", leaderboard.getScoreUnits());
        // for now, do not allow updating of firstEpochTimestamp or epochInterval

        final MongoLeaderboard mongoLeaderboard;

        try {
            mongoLeaderboard = datastore.findAndModify(query, updateOperations, new FindAndModifyOptions()
                .upsert(false)
                .returnNew(true));
        } catch (MongoCommandException ex) {
            if (ex.getErrorCode() == 11000) {
                throw new DuplicateException(ex);
            } else {
                throw new InternalException(ex);
            }
        }

        if (leaderboardNameOrId == null) {
            throw new LeaderboardNotFoundException("Leaderboard not found: " + leaderboardNameOrId);
        }

        objectIndex.index(mongoLeaderboard);
        return getBeanMapper().map(mongoLeaderboard, Leaderboard.class);

    }

    @Override
    public void deleteLeaderboard(final String leaderboardNameOrId) {

        final Query<MongoLeaderboard> query = datastore.createQuery(MongoLeaderboard.class);

        query.filter("active =", true);

        try {
            query.filter("_id", new ObjectId(leaderboardNameOrId));
        } catch (IllegalArgumentException ex) {
            query.filter("name =", leaderboardNameOrId);
        }

        final MongoLeaderboard mongoLeaderboard = getDatastore().findAndDelete(query, new FindAndModifyOptions()
            .remove(true)
            .returnNew(false));

        getObjectIndex().delete(mongoLeaderboard);

    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
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

    public Mapper getBeanMapper() {
        return beanMapper;
    }

    @Inject
    public void setBeanMapper(Mapper beanMapper) {
        this.beanMapper = beanMapper;
    }

}
