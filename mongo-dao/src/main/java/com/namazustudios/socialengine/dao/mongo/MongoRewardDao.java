package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.MongoException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.RewardDao;
import com.namazustudios.socialengine.dao.mongo.MongoConcurrentUtils.ContentionException;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItem;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoInventoryItemId;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoReward;
import com.namazustudios.socialengine.exception.*;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.User;
import com.namazustudios.socialengine.model.ValidationGroups;
import com.namazustudios.socialengine.model.inventory.InventoryItem;
import com.namazustudios.socialengine.model.mission.Reward;
import com.namazustudios.socialengine.model.mission.RewardIssuance;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.Key;
import org.mongodb.morphia.UpdateOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static com.google.common.base.Strings.nullToEmpty;
import static com.namazustudios.socialengine.dao.InventoryItemDao.SIMPLE_PRIORITY;
import static com.namazustudios.socialengine.dao.mongo.model.mission.MongoRewardIssuanceId.parseOrThrowNotFoundException;
import static java.lang.System.currentTimeMillis;
import static java.util.Collections.emptySet;
import static java.util.UUID.randomUUID;
import static java.util.stream.Collectors.toSet;
import static org.apache.commons.lang3.StringUtils.isEmpty;

public class MongoRewardDao implements RewardDao {

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private MongoUserDao mongoUserDao;

    private MongoItemDao mongoItemDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private ValidationHelper validationHelper;

    @Override
    public Reward getReward(final String id) {
        final MongoReward mongoReward = getMongoReward(id);
        return getDozerMapper().map(mongoReward, Reward.class);
    }

    public MongoReward getMongoReward(final String id) {
        if (isEmpty(nullToEmpty(id).trim())) {
            throw new NotFoundException("Unable to find reward with an id " + id);
        }

        final Query<MongoReward> query = datastore.createQuery(MongoReward.class);

        query.filter("_id", new ObjectId(id));

        final MongoReward mongoReward = query.get();

        if (mongoReward == null) {
            throw new NotFoundException("Reward " + id + " not found.");
        }

        return mongoReward;
    }

    @Override
    public Reward createReward(Reward reward) {
        getValidationHelper().validateModel(reward, ValidationGroups.Insert.class);

        final MongoReward mongoReward = getDozerMapper().map(reward, MongoReward.class);

        try {
            getDatastore().insert(mongoReward);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }

        final MongoReward fetchedMongoReward = getDatastore().get(mongoReward);
        return getDozerMapper().map(fetchedMongoReward, Reward.class);
    }

    @Override
    public void delete(String id) {
        final WriteResult writeResult = getDatastore().delete(MongoReward.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Reward not found: " + id);
        }
    }

    public Mapper getDozerMapper() {
        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {
        this.dozerMapper = dozerMapper;
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

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoItemDao getMongoItemDao() {
        return mongoItemDao;
    }

    @Inject
    public void setMongoItemDao(MongoItemDao mongoItemDao) {
        this.mongoItemDao = mongoItemDao;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public void validate(final Reward reward) {

        if (reward == null) {
            throw new InvalidDataException("Reward must not be null.");
        }

        validationHelper.validateModel(reward);

    }
}
