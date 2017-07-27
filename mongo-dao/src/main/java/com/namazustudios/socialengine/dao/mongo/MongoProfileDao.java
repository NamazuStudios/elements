package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.ValidationHelper;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoApplication;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.TooBusyException;
import com.namazustudios.socialengine.fts.ObjectIndex;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.profile.Profile;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.TermQuery;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import org.mongodb.morphia.AdvancedDatastore;
import org.mongodb.morphia.FindAndModifyOptions;
import org.mongodb.morphia.query.Query;
import org.mongodb.morphia.query.UpdateOperations;

import javax.inject.Inject;

import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 * Created by patricktwohig on 6/28/17.
 */
public class MongoProfileDao implements ProfileDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private AdvancedDatastore datastore;

    private Mapper beanMapper;

    private StandardQueryParser standardQueryParser;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    public Pagination<Profile> getActiveProfiles(int offset, int count) {

        final Query<MongoProfile> query;
        query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("active").equal(true)
        );

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> {

            if (!input.getUser().isActive()) {
                input.setUser(null);
            }

            if (!input.getApplication().isActive()) {
                input.setApplication(null);
            }

            return getBeanMapper().map(input, Profile.class);

        });

    }

    @Override
    public Pagination<Profile> getActiveProfiles(int offset, int count, String search) {

        final BooleanQuery booleanQuery = new BooleanQuery();

        try {
            final Term activeTerm = new Term("active", "true");
            booleanQuery.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);
            booleanQuery.add(getStandardQueryParser().parse(search, "name"), BooleanClause.Occur.FILTER);
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(MongoProfile.class, booleanQuery, offset, count, this::transform);

    }

    @Override
    public Profile getActiveProfile(String profileId) {
        final MongoProfile mongoProfile = getActiveMongoProfile(profileId);
        return getBeanMapper().map(mongoProfile, Profile.class);

    }

    public MongoProfile getActiveMongoProfile(String profileId) {

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);
        final ObjectId objectId = getMongoDBUtils().parse(profileId);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("_id").equal(objectId)
        );

        final MongoProfile mongoProfile = query.get();

        if (mongoProfile == null) {
            throw new NotFoundException("No profile exists with id " + profileId);
        }

        return mongoProfile;
    }

    @Override
    public Profile getActiveProfile(final String userId, final String applicationId) {

        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(userId);
        final MongoApplication mongoApplication = getMongoApplicationDao().getActiveMongoApplication(applicationId);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("user").equal(mongoUser),
            query.criteria("application").equal(mongoApplication)
        );

        final MongoProfile mongoProfile = query.get();

        if (mongoProfile == null) {
            throw new NotFoundException("no matching profile for user " + userId + " and application " + applicationId);
        }

        return transform(mongoProfile);

    }

    @Override
    public Profile updateActiveProfile(Profile profile) {

        validate(profile);

        final ObjectId objectId = getMongoDBUtils().parse(profile.getId());
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("_id").equal(objectId)
        );

        final UpdateOperations<MongoProfile> updateOperations = datastore.createUpdateOperations(MongoProfile.class);

        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        updateOperations.set("displayName", nullToEmpty(profile.getDisplayName()).trim());

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                .upsert(false)
                .returnNew(true);

            return ds.findAndModify(query, updateOperations, findAndModifyOptions);

        });

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profile.getId());
        }

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    @Override
    public Profile createOrReactivateProfile(Profile profile) {

        validate(profile);

        final ObjectId objectId = getMongoDBUtils().parse(profile.getId());
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("_id").equal(objectId),
            query.criteria("user").equal(user),
            query.criteria("application").equal(application)
        );

        final UpdateOperations<MongoProfile> updateOperations = datastore.createUpdateOperations(MongoProfile.class);

        updateOperations.set("user", user);
        updateOperations.set("active", true);
        updateOperations.set("application", application);
        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        updateOperations.set("displayName", nullToEmpty(profile.getDisplayName()).trim());

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                    .upsert(true)
                    .returnNew(true);

            return ds.findAndModify(query, updateOperations, findAndModifyOptions);

        });

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profile.getId());
        }

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    @Override
    public Profile createReactivateOrRefreshProfile(Profile profile) {

        validate(profile);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.and(
            query.criteria("user").equal(user),
            query.criteria("application").equal(application)
        );

        final MongoProfile mongoProfile;

        try {

            mongoProfile = getMongoConcurrentUtils().performOptimisticUpsert(query, (datastore, toUpsert) -> {

                if (toUpsert.getObjectId() != null) {
                    profile.setId(toUpsert.getObjectId().toHexString());
                }

                if (toUpsert.isActive()) {
                    // The only thing to refresh here is the
                    toUpsert.setImageUrl(profile.getImageUrl());
                } else {
                    toUpsert.setActive(true);
                    getBeanMapper().map(profile, toUpsert);
                }

                return toUpsert;

            });
        } catch (MongoConcurrentUtils.ConflictException e) {
            throw new TooBusyException(e);
        }

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profile.getId());
        }

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    private MongoUser getMongoUserFromProfile(final Profile profile) {
        return getMongoUserDao().getActiveMongoUser(profile.getUser().getName());
    }

    private MongoApplication getMongoApplicationFromProfile(final Profile profile) {
        return getMongoApplicationDao().getActiveMongoApplication(profile.getApplication().getId());
    }

    @Override
    public void softDeleteProfile(String profileId) {

        final ObjectId objectId = getMongoDBUtils().parse(profileId);
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("_id").equal(objectId)
        );

        final UpdateOperations<MongoProfile> updateOperations = datastore.createUpdateOperations(MongoProfile.class);
        updateOperations.set("active", false);

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                    .upsert(false)
                    .returnNew(true);

            return ds.findAndModify(query, updateOperations, findAndModifyOptions);

        });

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profileId);
        }

        getObjectIndex().index(mongoProfile);

    }

    private void validate(final Profile profile) {


        if (profile == null) {
            throw new InvalidDataException("application must not be null.");
        }

        validationHelper.validateModel(profile);

    }

    public Profile transform(final MongoProfile input) {

        if (!input.getUser().isActive()) {
            input.setUser(null);
        }

        if (!input.getApplication().isActive()) {
            input.setApplication(null);
        }

        return getBeanMapper().map(input, Profile.class);

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

    public StandardQueryParser getStandardQueryParser() {
        return standardQueryParser;
    }

    @Inject
    public void setStandardQueryParser(StandardQueryParser standardQueryParser) {
        this.standardQueryParser = standardQueryParser;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MongoApplicationDao getMongoApplicationDao() {
        return mongoApplicationDao;
    }

    @Inject
    public void setMongoApplicationDao(MongoApplicationDao mongoApplicationDao) {
        this.mongoApplicationDao = mongoApplicationDao;
    }

    public ObjectIndex getObjectIndex() {
        return objectIndex;
    }

    @Inject
    public void setObjectIndex(ObjectIndex objectIndex) {
        this.objectIndex = objectIndex;
    }

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

}
