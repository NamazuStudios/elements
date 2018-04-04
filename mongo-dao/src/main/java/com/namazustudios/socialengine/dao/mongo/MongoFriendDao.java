package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.FriendDao;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.friend.Friend;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;

import javax.inject.Inject;

public class MongoFriendDao implements FriendDao {

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private ObjectIndex objectIndex;

    private StandardQueryParser standardQueryParser;

    private MongoDBUtils mongoDBUtils;

    private Mapper dozerMapper;

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset, final int count) {
        return null;
    }

    @Override
    public Pagination<Friend> getFriendsForUser(final User user, final int offset,final int count, final String search) {
        return null;
    }

    @Override
    public Friend getFriendForUser(final User user, final String friendId) {
        return null;
    }

    @Override
    public void deleteFriendForUser(final User user, final String friendId) {

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

}
