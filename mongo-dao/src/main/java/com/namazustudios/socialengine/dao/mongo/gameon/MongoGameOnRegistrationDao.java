package com.namazustudios.socialengine.dao.mongo.gameon;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.GameOnRegistrationDao;
import com.namazustudios.socialengine.dao.mongo.MongoDBUtils;
import com.namazustudios.socialengine.dao.mongo.MongoProfileDao;
import com.namazustudios.socialengine.dao.mongo.MongoUserDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.gameon.MongoGameOnRegistration;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.gameon.GameOnRegistrationNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.gameon.game.GameOnRegistration;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.query.Query;

import javax.inject.Inject;

public class MongoGameOnRegistrationDao implements GameOnRegistrationDao {

    private Mapper mapper;

    private MongoDBUtils mongoDBUtils;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    private ValidationHelper validationHelper;

    private AdvancedDatastore advancedDatastore;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    @Override
    public GameOnRegistration getRegistrationForProfile(final Profile profile) {

        final MongoProfile mongoProfile;
        mongoProfile = getMongoProfileDao().getActiveMongoProfile(profile);

        final MongoGameOnRegistration mongoGameOnRegistration;
        mongoGameOnRegistration = getAdvancedDatastore().get(MongoGameOnRegistration.class, mongoProfile.getObjectId());

        if (mongoGameOnRegistration == null) {
            throw new GameOnRegistrationNotFoundException("Registration not found: " + profile.getId());
        }

        return getMapper().map(mongoGameOnRegistration, GameOnRegistration.class);

    }

    @Override
    public GameOnRegistration getRegistrationForUser(final User user, final String gameOnRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(gameOnRegistrationId);

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnRegistration> query = getAdvancedDatastore().createQuery(MongoGameOnRegistration.class);

        query.and(
            query.criteria("_id").equal(registrationId),
            query.criteria("user").equal(mongoUser)
        );

        final MongoGameOnRegistration mongoGameOnRegistration = query.get();

        if (mongoGameOnRegistration == null) {
            throw new GameOnRegistrationNotFoundException("Registration not found " + gameOnRegistrationId);
        }

        return getMapper().map(mongoGameOnRegistration, GameOnRegistration.class);

    }

    @Override
    public Pagination<GameOnRegistration> getRegistrationsForUser(final User user, final int offset, final int count) {
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnRegistration> query = getAdvancedDatastore().createQuery(MongoGameOnRegistration.class);
        query.field("user").equal(mongoUser);
        return getMongoDBUtils().paginationFromQuery(query, offset, count, GameOnRegistration.class);
    }

    @Override
    @SuppressWarnings("Duplicates")
    public Pagination<GameOnRegistration> getRegistrationsForUser(final User user,
                                                                  final int offset, final int count,
                                                                  final String queryString) {

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
            MongoGameOnRegistration.class, booleanQueryBuilder.build(),
            offset, count,
            GameOnRegistration.class);

    }

    @Override
    public GameOnRegistration createRegistration(final GameOnRegistration gameOnRegistration) {

        getValidationHelper().validateModel(gameOnRegistration, Insert.class);

        final MongoProfile mongoProfile = getMongoProfileDao().getActiveMongoProfile(gameOnRegistration.getProfile());

        final MongoGameOnRegistration mongoGameOnRegistration = new MongoGameOnRegistration();
        mongoGameOnRegistration.setObjectId(mongoProfile.getObjectId());
        mongoGameOnRegistration.setUser(mongoProfile.getUser());
        mongoGameOnRegistration.setProfile(mongoProfile);
        mongoGameOnRegistration.setPlayerToken(gameOnRegistration.getPlayerToken().trim());
        mongoGameOnRegistration.setExternalPlayerId(gameOnRegistration.getExternalPlayerId().trim());

        try {
            getAdvancedDatastore().insert(mongoGameOnRegistration);
        } catch (DuplicateKeyException ex) {
            throw new DuplicateException("Registration already exists for profile: " + mongoProfile.getObjectId(), ex);
        }

        getObjectIndex().index(mongoGameOnRegistration);

        return getMapper().map(mongoGameOnRegistration, GameOnRegistration.class);

    }

    @Override
    public void deleteRegistrationForUser(final User user, final String gameOnRegistrationId) {

        final ObjectId registrationId = getMongoDBUtils().parseOrThrowNotFoundException(gameOnRegistrationId);

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user);
        final Query<MongoGameOnRegistration> query = getAdvancedDatastore().createQuery(MongoGameOnRegistration.class);

        query.and(
            query.criteria("_id").equal(registrationId),
            query.criteria("user").equal(mongoUser)
        );

        final WriteResult writeResult = getAdvancedDatastore().delete(query);

        if (writeResult.getN() == 0) {
            throw new GameOnRegistrationNotFoundException("Registration not found " + gameOnRegistrationId);
        } else {
            getObjectIndex().delete(MongoGameOnRegistration.class, registrationId);
        }

    }

    @Override
    public GameOnRegistration getRegistrationForExternalPlayerId(final String externalPlayerId) {

        final Query<MongoGameOnRegistration> query = getAdvancedDatastore()
            .createQuery(MongoGameOnRegistration.class)
            .field("externalPlayerId").equal(externalPlayerId);

        final MongoGameOnRegistration mongoGameOnRegistration = query.get();

        if (mongoGameOnRegistration == null ||
            !mongoGameOnRegistration.getProfile().isActive() ||
            !mongoGameOnRegistration.getProfile().getUser().isActive()) {
            throw new GameOnRegistrationNotFoundException("Registration not found for external player ID: " + externalPlayerId);
        }

        return getMapper().map(mongoGameOnRegistration, GameOnRegistration.class);

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
