package com.namazustudios.socialengine.dao.mongo.gameon;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.GameOnSessionDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnSession;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnSessionId;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.gameon.GameOnSessionNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.gameon.game.DeviceOSType;
import com.namazustudios.socialengine.model.gameon.game.GameOnSession;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.query.Query;

import javax.inject.Inject;
import java.sql.Timestamp;

public class MongoGameOnSessionDao implements GameOnSessionDao {

    private Mapper mapper;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private AdvancedDatastore advancedDatastore;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    @Override
    public Pagination<GameOnSession> getSessionsForUser(final User user, final int offset, final int count) {
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnSession> query = getAdvancedDatastore().createQuery(MongoGameOnSession.class);
        query.field("user").equal(mongoUser);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, GameOnSession.class);
    }

    @SuppressWarnings("Duplicates")
    @Override
    public Pagination<GameOnSession> getSessionsForUser(final User user, final int offset,
                                                        final int count, final String queryString) {

        getMongoUserDao().getActiveMongoUser(user);  // Checks that the user even exists before doing anything else.

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {
            final Term userTerm = new Term("userId", user.getId());
            final org.apache.lucene.search.Query searchQuery = getStandardQueryParser().parse(queryString, "displayName");
            booleanQueryBuilder.add(new TermQuery(userTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(searchQuery, BooleanClause.Occur.FILTER);
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(
            MongoGameOnSession.class, booleanQueryBuilder.build(),
            offset, count,
            GameOnSession.class);

    }

    @Override
    public GameOnSession getSessionForUser(final User user, final String gameOnSessionId) {

        final MongoGameOnSessionId sessionId;

        try {
            sessionId = new MongoGameOnSessionId(gameOnSessionId);
        } catch (IllegalArgumentException ex) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + gameOnSessionId);
        }

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnSession> query = getAdvancedDatastore().createQuery(MongoGameOnSession.class);

        query.and(
            query.criteria("_id").equal(sessionId),
            query.criteria("user").equal(mongoUser)
        );

        final MongoGameOnSession mongoGameOnSession = query.get();

        if (mongoGameOnSession == null) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + gameOnSessionId);
        }

        return getMapper().map(mongoGameOnSession, GameOnSession.class);

    }

    @Override
    public GameOnSession getSessionForProfile(final Profile profile, final DeviceOSType deviceOSType) {

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);
        final Query<MongoGameOnSession> query = getAdvancedDatastore().createQuery(MongoGameOnSession.class);

        query.and(
            query.criteria("profile").equal(mongoProfile),
            query.criteria("_id.deviceOSType").equal(deviceOSType)
        );

        final MongoGameOnSession mongoGameOnSession = query.get();

        if (mongoGameOnSession == null) {
            throw new GameOnSessionNotFoundException("GameOn session not found for profile and OS " + profile.getId() + " " + deviceOSType);
        }

        return getMapper().map(mongoGameOnSession, GameOnSession.class);

    }

    @Override
    public void deleteSessionForUser(final User user, final String gameOnSessionId) {

        final MongoGameOnSessionId sessionId;

        try {
            sessionId = new MongoGameOnSessionId(gameOnSessionId);
        } catch (IllegalArgumentException ex) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + gameOnSessionId);
        }

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnSession> query = getAdvancedDatastore().createQuery(MongoGameOnSession.class);

        query.and(
            query.criteria("_id").equal(sessionId),
            query.criteria("user").equal(mongoUser)
        );

        final WriteResult writeResult = getAdvancedDatastore().delete(query);

        if (writeResult.getN() == 0) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + gameOnSessionId);
        } else {
            getObjectIndex().delete(MongoGameOnSession.class, gameOnSessionId);
        }

    }

    @Override
    public void deleteSession(final String id) {
        final MongoGameOnSessionId sessionId;

        try {
            sessionId = new MongoGameOnSessionId(id);
        } catch (IllegalArgumentException ex) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + id);
        }

        final WriteResult writeResult = getAdvancedDatastore().delete(MongoGameOnSession.class, sessionId);

        if (writeResult.getN() == 0) {
            throw new GameOnSessionNotFoundException("GameOn session not found " + id);
        } else {
            getObjectIndex().delete(MongoGameOnSession.class, id);
        }

    }

    @Override
    public GameOnSession createSession(final GameOnSession authenticated) {

        getValidationHelper().validateModel(authenticated, Insert.class);

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(authenticated.getProfile());

        final MongoGameOnSession mongoGameOnSession = new MongoGameOnSession();

        final MongoGameOnSessionId mongoGameOnSessionId;
        mongoGameOnSessionId = new MongoGameOnSessionId(mongoProfile.getObjectId(), authenticated.getDeviceOSType());

        // Indexed and internal properties
        mongoGameOnSession.setObjectId(mongoGameOnSessionId);
        mongoGameOnSession.setProfile(mongoProfile);
        mongoGameOnSession.setUser(mongoProfile.getUser());

        // Properties carried over by Amazon GameOn
        mongoGameOnSession.setAppBuildType(authenticated.getAppBuildType());
        mongoGameOnSession.setSessionId(authenticated.getSessionId().trim());
        mongoGameOnSession.setSessionApiKey(authenticated.getSessionApiKey().trim());

        final Timestamp timestamp = new Timestamp(authenticated.getSessionExpirationDate());
        mongoGameOnSession.setSessionExpirationDate(timestamp);

        try {
            getAdvancedDatastore().insert(mongoGameOnSession);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("Session already exists for profile and OS " +
                                         mongoProfile.getObjectId() + " " + authenticated.getDeviceOSType(), ex);
        }

        return getMapper().map(mongoGameOnSession, GameOnSession.class);

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoProfileDao getMongoProfileDao() {
        return mongoProfileDao;
    }

    @Inject
    public void setMongoProfileDao(MongoProfileDao mongoProfileDao) {
        this.mongoProfileDao = mongoProfileDao;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public AdvancedDatastore getAdvancedDatastore() {
        return advancedDatastore;
    }

    @Inject
    public void setAdvancedDatastore(AdvancedDatastore advancedDatastore) {
        this.advancedDatastore = advancedDatastore;
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

}
