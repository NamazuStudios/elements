package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.MissionDao;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.mission.Mission;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

public class MongoMissionDao implements MissionDao {

    private static final Logger LOGGER = LoggerFactory.getLogger(MongoMissionDao.class);

    private StandardQueryParser standardQueryParser;

    private ObjectIndex objectIndex;

    private AdvancedDatastore datastore;

    private ValidationHelper validationHelper;

    private Mapper dozerMapper;

    private MongoDBUtils mongoDBUtils;

    @Override
    public Pagination<Mission> getMissions(int offset, int count) {
        return null;
    }

    @Override
    public Pagination<Mission> getMissions(int offset, int count, String search) {
        return null;
    }

    @Override
    public Mission getMission(String missionId) {
        return null;
    }

    @Override
    public Mission updateMission(Mission mission) {
        return null;
    }

    @Override
    public Mission createMission(Mission mission) {
        return null;
    }

    @Override
    public void deleteMission(String missionId) {

    }


    private void validate(Mission item) {
        if (item == null) {
            throw new InvalidDataException("Inventory item must not be null.");
        }
        getValidationHelper().validateModel(item);
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
