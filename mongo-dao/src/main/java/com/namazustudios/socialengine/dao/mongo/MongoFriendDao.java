package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.WriteResult;
import com.namazustudios.socialengine.dao.FriendDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendship;
import com.namazustudios.socialengine.dao.mongo.model.MongoFriendshipId;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.FriendNotFoundException;
import com.namazustudios.socialengine.exception.InternalException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.Friend;
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
import java.util.List;
import java.util.stream.Stream;

import static com.namazustudios.socialengine.model.friend.Friendship.*;
import static java.util.stream.Collectors.toList;

public class MongoFriendDao implements FriendDao {

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());
        final Query<MongoFriendship> query = getDatastore().createQuery(MongoFriendship.class);

        query.and(
            query.or(
                query.criteria("_id.lesser").equal(user.getId()),
                query.criteria("_id.greater").equal(user.getId())
            ),
            query.or(
                query.criteria("lesserAccepted").equal(true),
                query.criteria("greaterAccepted").equal(true)
            )
        );

        return getMongoDBUtils().paginationFromQuery(query, offset, count, f -> transform(mongoUser, f));

    }

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count, final String search) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());

        try {
            final Term userTerm = new Term("user", user.getId());
            booleanQueryBuilder.add(new TermQuery(userTerm), BooleanClause.Occur.FILTER);
            booleanQueryBuilder.add(getStandardQueryParser().parse(search, "user"), BooleanClause.Occur.FILTER);
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoFriendship.class, booleanQueryBuilder.build(), offset, count, f -> transform(mongoUser, f));

    }

    @Override
    public Friend getFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex -> new FriendNotFoundException(ex));

        final Query<MongoFriendship> query = getDatastore().createQuery(MongoFriendship.class);

        query.and(
            query.criteria("_id").equal(mongoFriendshipId),
            query.or(
                query.criteria("lesserAccepted").equal(true),
                query.criteria("greaterAccepted").equal(true)
            )
        );

        final MongoFriendship mongoFriendship = getDatastore().get(MongoFriendship.class, mongoFriendshipId);
        return transform(mongoUser, mongoFriendship);

    }

    @Override
    public void deleteFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());
        final ObjectId mongoUserId = mongoUser.getObjectId();

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex ->
            new FriendNotFoundException("Friend not found: " + friendId, ex));

        final Query<MongoFriendship> query = getDatastore().createQuery(MongoFriendship.class);

        final String property;

        if (mongoFriendshipId.getLesser().equals(mongoUserId)) {
            property = "lesserAccepted";
        } else if (mongoFriendshipId.getGreater().equals(mongoUserId)) {
            property = "greaterAccepted";
        } else {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        }

        query.and(
            query.criteria("_id").equal(mongoFriendshipId),
            query.criteria(property).equal(true)
        );

        final WriteResult writeResult = getDatastore().delete(query);

        if (writeResult.getN() == 0) {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        } else if (writeResult.getN() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    private Friend transform(final MongoUser mongoUser, final MongoFriendship mongoFriendship) {

        final ObjectId lesserObjectId = mongoFriendship.getObjectId().getLesser();
        final ObjectId greaterObjectId = mongoFriendship.getObjectId().getGreater();

        if (mongoFriendship.isLesserAccepted() && mongoFriendship.isGreaterAccepted()) {
            mongoFriendship.setFriendship(MUTUAL);
        } else if (lesserObjectId.equals(mongoUser.getObjectId())) {
            mongoFriendship.setFriendship(mongoFriendship.isLesserAccepted() ? OUTGOING : INCOMING);
        } else if (greaterObjectId.equals(mongoUser.getObjectId())) {
            mongoFriendship.setFriendship(mongoFriendship.isGreaterAccepted() ? OUTGOING : INCOMING);
        } else {
            mongoFriendship.setFriendship(NONE);
        }

        final MongoUser lesser = getDatastore().get(MongoUser.class, lesserObjectId);
        final MongoUser greater = getDatastore().get(MongoUser.class, greaterObjectId);

        final List<MongoProfile> profiles = Stream.of(lesser, greater)
            .filter(u -> u != null && !u.equals(mongoUser))
            .flatMap(u -> getMongoProfileDao().getActiveMongoProfilesForUser(mongoUser))
            .collect(toList());

        mongoFriendship.setProfiles(profiles);

        return getDozerMapper().map(mongoFriendship, Friend.class);

    }

    public AdvancedDatastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {
        this.datastore = datastore;
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

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
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

}
