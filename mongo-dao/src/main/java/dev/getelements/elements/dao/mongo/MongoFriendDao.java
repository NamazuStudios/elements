package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.dao.FriendDao;
import dev.getelements.elements.dao.mongo.model.MongoFriendship;
import dev.getelements.elements.dao.mongo.model.MongoFriendshipId;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.exception.FriendNotFoundException;
import dev.getelements.elements.exception.InternalException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.friend.Friend;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;

import javax.inject.Inject;
import java.util.List;

import static dev.getelements.elements.model.friend.Friendship.*;
import static java.util.stream.Collectors.toList;

public class MongoFriendDao implements FriendDao {

    private Datastore datastore;

    private ValidationHelper validationHelper;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    private MongoUserDao mongoUserDao;

    private MongoProfileDao mongoProfileDao;

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());
        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(
                Filters.or(
                        Filters.eq("_id.lesser", mongoUser.getObjectId()),
                        Filters.eq("_id.greater", mongoUser.getObjectId())
                ),
                Filters.or(
                        Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true)
                )
        ));

        return getMongoDBUtils().paginationFromQuery(query, offset, count, f -> transform(mongoUser, f), new FindOptions());

    }

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count, final String search) {
        //TODO Fix This
        return Pagination.empty();
    }

    @Override
    public Friend getFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex -> new FriendNotFoundException(ex));

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(Filters.eq("_id", mongoFriendshipId),
                Filters.or(Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true))));

        final MongoFriendship mongoFriendship = query.first();
        return transform(mongoUser, mongoFriendship);

    }

    public List<MongoFriendship> getAllMongoFriendshipsForUser(final MongoUser mongoUser) {

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        query.filter(Filters.and(
                Filters.or(
                        Filters.eq("_id.lesser", mongoUser.getObjectId()),
                        Filters.eq("_id.greater", mongoUser.getObjectId())
                        ),
                Filters.or(
                        Filters.eq("lesserAccepted", true),
                        Filters.eq("greaterAccepted", true)
                )
        ));

        return query.iterator().toList();

    }

    @Override
    public void deleteFriendForUser(final User user, final String friendId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(user.getId());
        final ObjectId mongoUserId = mongoUser.getObjectId();

        final MongoFriendshipId mongoFriendshipId;
        mongoFriendshipId = MongoFriendshipId.parseOrThrow(friendId, ex ->
            new FriendNotFoundException("Friend not found: " + friendId, ex));

        final Query<MongoFriendship> query = getDatastore().find(MongoFriendship.class);

        final String property;

        if (mongoFriendshipId.getLesser().equals(mongoUserId)) {
            property = "lesserAccepted";
        } else if (mongoFriendshipId.getGreater().equals(mongoUserId)) {
            property = "greaterAccepted";
        } else {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        }

        query.filter(Filters.and(
                Filters.eq("_id", mongoFriendshipId),
                Filters.eq(property, true)
        ));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new FriendNotFoundException("Friend not found: " + friendId);
        } else if (deleteResult.getDeletedCount() > 1) {
            throw new InternalException("Deleted more rows than expected.");
        }

    }

    private Friend transform(final MongoUser mongoUser, final MongoFriendship mongoFriendship) {

        final Friend friend = getDozerMapper().map(mongoFriendship, Friend.class);

        final ObjectId lesserObjectId = mongoFriendship.getObjectId().getLesser();
        final ObjectId greaterObjectId = mongoFriendship.getObjectId().getGreater();

        if (mongoFriendship.isLesserAccepted() && mongoFriendship.isGreaterAccepted()) {
            friend.setFriendship(MUTUAL);
        } else if (lesserObjectId.equals(mongoUser.getObjectId())) {
            friend.setFriendship(mongoFriendship.isLesserAccepted() ? OUTGOING : INCOMING);
        } else if (greaterObjectId.equals(mongoUser.getObjectId())) {
            friend.setFriendship(mongoFriendship.isGreaterAccepted() ? OUTGOING : INCOMING);
        } else {
            friend.setFriendship(NONE);
        }

        final MongoUser lesser = getDatastore().find(MongoUser.class).filter("_id.lesser", lesserObjectId).first();
        final MongoUser greater = getDatastore().find(MongoUser.class).filter("_id.greater", greaterObjectId).first();
        final MongoUser friendUser = lesser.equals(mongoUser) ? greater : lesser;

        friend.setUser(getDozerMapper().map(friendUser, User.class));
        friend.setProfiles(getMongoProfileDao()
            .getActiveMongoProfilesForUser(friendUser)
            .map(p -> getDozerMapper().map(p, Profile.class))
            .collect(toList()));

        return friend;

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
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
