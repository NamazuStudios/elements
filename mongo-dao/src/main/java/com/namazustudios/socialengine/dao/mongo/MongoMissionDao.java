package com.namazustudios.socialengine.dao.mongo;

import com.mongodb.DuplicateKeyException;
import com.mongodb.WriteResult;
import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.dao.mongo.model.mission.MongoMission;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.commons.lang3.StringUtils;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;
import javax.inject.Singleton;

@Singleton
public class MongoMissionDao implements MissionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoMissionDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Pagination<Mission> getMissions(int offset, int count)  { return getMissions(offset, count, null); }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, String search) {
        if (StringUtils.isNotEmpty(search)) {
            LOGGER.warn(" getMissions(int offset, int count, String query) was called with a query " +
                    "string parameter.  This field is presently ignored and will return all values");
        }

        final Query<MongoMission> query = getDatastore().createQuery(MongoMission.class);

        return getMongoDBUtils().paginationFromQuery(query, offset, count,
            mongoItem -> getDozerMapper().map(mongoItem, Mission.class));

    }

    @Override
    public Mission getMissionByNameOrId(String identifier) {
        if (StringUtils.isEmpty(identifier)) {
            throw new NotFoundException("Unable to find mission with an id or name of " + identifier);
        }

        Query<MongoMission> query = getDatastore().createQuery(MongoMission.class);

        if (ObjectId.isValid(identifier)) {
            query.criteria("_id").equal(new ObjectId(identifier));
        } else {
            query.criteria("name").equal(identifier);
        }

        final MongoMission mission = query.get();

        if (mission == null) {
            throw new NotFoundException("Unable to find item with an id or name of " + identifier);
        }

        return getDozerMapper().map(mission, Mission.class);
    }

    @Override
    public Mission updateMission(Mission mission) {
        getValidationHelper().validateModel(mission, Update.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(mission.getId());

        final Query<MongoMission> query = getDatastore().createQuery(MongoMission.class);
        query.criteria("_id").equal(objectId);

        final UpdateOperations<MongoMission> operations = getDatastore().createUpdateOperations(MongoMission.class);
        operations.set("name", mission.getName());
        operations.set("displayName", mission.getDisplayName());
        operations.set("description", mission.getDescription());
        operations.set("steps", mission.getSteps());
        operations.set("getFinalRepeatStep", mission.getFinalRepeatStep());

        final FindAndModifyOptions options = new FindAndModifyOptions()
            .returnNew(true)
            .upsert(false);

        final MongoMission updatedMongoItem = getDatastore().findAndModify(query, operations, options);
        if (updatedMongoItem == null) {
            throw new NotFoundException("Mission with id or name of " + mission.getId() + " does not exist");
        }

        getObjectIndex().index(updatedMongoItem);

        return getDozerMapper().map(updatedMongoItem, Mission.class);
    }

    @Override
    public Mission createMission(Mission mission) {
        getValidationHelper().validateModel(mission, Insert.class);

        normalize(mission);

        final MongoMission mongoItem = getDozerMapper().map(mission, MongoMission.class);

        try {
            getDatastore().save(mongoItem);
        } catch (DuplicateKeyException e) {
            throw new DuplicateException(e);
        }
        getObjectIndex().index(mongoItem);

        return getDozerMapper().map(getDatastore().get(mongoItem), Mission.class);
    }

    @Override
    public void deleteMission(String missionId) {
        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(missionId);
        final WriteResult writeResult = getDatastore().delete(MongoMission.class, id);

        if (writeResult.getN() == 0) {
            throw new NotFoundException("Mission not found: " + missionId);
        }
    }


    private void normalize(Mission item) {
        // leave this stub here in case we implement some normalization logic later
    }

    public AdvancedDatastore getDatastore() {

        return datastore;
    }

    @Inject
    public void setDatastore(AdvancedDatastore datastore) {

        this.datastore = datastore;
    }

    public Mapper getDozerMapper() {

        return dozerMapper;
    }

    @Inject
    public void setDozerMapper(Mapper dozerMapper) {

        this.dozerMapper = dozerMapper;
    }

    public ValidationHelper getValidationHelper() {

        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {

        this.validationHelper = validationHelper;
    }

    public MongoDBUtils getMongoDBUtils() {

        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {

        this.mongoDBUtils = mongoDBUtils;
    }


    public StandardQueryParser getStandardQueryParser() {

        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
    }

    public ObjectIndex getObjectIndex() {

        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {

        this.objectIndex = objectIndex;
    }

}
