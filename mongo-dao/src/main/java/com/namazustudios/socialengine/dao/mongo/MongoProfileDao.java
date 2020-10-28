package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.util.ValidationHelper;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.AdvancedDatastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.*;
import dev.morphia.query.Query;

import javax.inject.Inject;
import java.util.Date;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

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

    @Override
    public Optional<Profile> findActiveProfile(final String profileId) {

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);
        if (!ObjectId.isValid(profileId)) throw new ProfileNotFoundException();

        query.and(
            query.criteria("_id").equal(new ObjectId(profileId)),
            query.criteria("active").equal(true)
        );

        final MongoProfile mongoProfile = query.get();
        return mongoProfile == null ? Optional.empty() : Optional.of(transform(mongoProfile));

    }

    @Override
    public Optional<Profile> findActiveProfileForUser(final String profileId, final String userId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(userId);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);
        query.field("_id").equal(objectId);
        query.field("active").equal(true);
        query.field("user").equal(mongoUser);

        final MongoProfile mongoProfile = query.get();
        return mongoProfile == null ? Optional.empty() : Optional.of(transform(mongoProfile));

    }

    public Pagination<Profile> getActiveProfiles(
            final int offset,
            final int count,
            final String applicationNameOrId, final String userId,
            final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(query.criteria("active").equal(true));

        if (lowerBoundTimestamp != null && upperBoundTimestamp != null && lowerBoundTimestamp > upperBoundTimestamp) {
            throw new IllegalArgumentException("Invalid range: upper bound should be less than or " +
                    "equal to lower bound.");
        }

        if (lowerBoundTimestamp != null) {

            final Date lowerBoundDate;

            if (lowerBoundTimestamp >= 0) {
                lowerBoundDate = new Date(lowerBoundTimestamp);
            } else {
                lowerBoundDate = new Date(0);
            }

            query.and(query.criteria("lastLogin").greaterThanOrEq(lowerBoundDate));
        }

        if (upperBoundTimestamp != null) {

            final Date upperBoundDate;

            if (upperBoundTimestamp >= 0) {
                upperBoundDate = new Date(upperBoundTimestamp);
            } else {
                upperBoundDate = new Date();
            }

            query.and(query.criteria("lastLogin").lessThanOrEq(upperBoundDate));

        }

        if (applicationNameOrId != null) {
            final MongoApplication mongoApplication;
            mongoApplication = getMongoApplicationDao().findActiveMongoApplication(applicationNameOrId);
            if (mongoApplication == null) return new Pagination<>();
            query.and(query.criteria("application").equal(mongoApplication));
        }

        if (userId != null) {
            final MongoUser mongoUser = getMongoUserDao().findActiveMongoUser(userId);
            if (mongoUser == null) return new Pagination<>();
            query.and(query.criteria("user").equal(mongoUser));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input));

    }

    @Override
    public Pagination<Profile> getActiveProfiles(
            final int offset,
            final int count,
            final String search) {

        final BooleanQuery.Builder booleanQueryBuilder = new BooleanQuery.Builder();

        try {
            booleanQueryBuilder.add(new DocValuesFieldExistsQuery("active"), BooleanClause.Occur.FILTER);
            final Term activeTerm = new Term("active", "true");
            booleanQueryBuilder.add(new TermQuery(activeTerm), BooleanClause.Occur.FILTER);

            if (search != null && search.length() > 0) {
                booleanQueryBuilder.add(
                        getStandardQueryParser().parse(search, "name"),
                        BooleanClause.Occur.FILTER
                );
            }
        } catch (QueryNodeException ex) {
            throw new BadQueryException(ex);
        }

        return getMongoDBUtils().paginationFromSearch(
                MongoProfile.class,
                booleanQueryBuilder.build(),
                offset,
                count,
                this::transform);

    }

    private static String buildDateString(long timestamp) {
        return DateTools.dateToString(new Date(timestamp), DateTools.Resolution.MILLISECOND);
    }

    @Override
    public Profile getActiveProfile(String profileId) {
        final MongoProfile mongoProfile = getActiveMongoProfile(profileId);
        return getBeanMapper().map(mongoProfile, Profile.class);

    }

    public MongoProfile getActiveMongoProfile(final Profile profile) {
        return getActiveMongoProfile(profile.getId());
    }

    public MongoProfile getActiveMongoProfile(final String profileId) {

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("_id").equal(objectId)
        );

        final MongoProfile mongoProfile = query.get();

        if (mongoProfile == null) {
            throw new ProfileNotFoundException("No profile exists with id " + profileId);
        }

        return mongoProfile;
    }

    public Stream<MongoProfile> getActiveMongoProfilesForUser(final MongoUser user) {

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("user").equal(user),
            query.criteria("active").equal(true)
        );

        return query.asList().stream();

    }

    public Stream<MongoProfile> getActiveMongoProfilesForUser(final ObjectId mongoUserObjectId) {
        final MongoUser mongoUser = new MongoUser();
        mongoUser.setObjectId(mongoUserObjectId);
        return getActiveMongoProfilesForUser(mongoUser);
    }

    @Override
    public Profile updateActiveProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Update.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profile.getId());
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
            query.criteria("active").equal(true),
            query.criteria("_id").equal(objectId)
        );

        final UpdateOperations<MongoProfile> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);
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
    public Profile updateActiveProfile(final Profile profile, final Map<String, Object> metadata) {

        getValidationHelper().validateModel(profile, Update.class);

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profile.getId());
        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("_id").equal(objectId)
        );

        final UpdateOperations<MongoProfile> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);
        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        updateOperations.set("displayName", nullToEmpty(profile.getDisplayName()).trim());

        if (metadata == null) {
            updateOperations.unset("metadata");
        } else {
            updateOperations.set("metadata", metadata);
        }

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
    public Profile updateMetadata(final String profileId, final Map<String, Object> metadata) {
        final MongoProfile mongoProfile = getActiveMongoProfile(profileId);
        return doUpdateMetadata(mongoProfile.getObjectId(), metadata);
    }

    @Override
    public Profile updateMetadata(final Profile profile, final Map<String, Object> metadata) {
        final MongoProfile mongoProfile = getActiveMongoProfile(profile);
        return doUpdateMetadata(mongoProfile.getObjectId(), metadata);
    }

    private Profile doUpdateMetadata(final ObjectId objectId, final Map<String, Object> metadata) {

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        query.and(
                query.criteria("active").equal(true),
                query.criteria("_id").equal(objectId)
        );

        final UpdateOperations<MongoProfile> updateOperations;
        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);

        if (metadata == null) {
            updateOperations.unset("metadata");
        } else {
            updateOperations.set("metadata", metadata);
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                    .upsert(false)
                    .returnNew(true);

            return ds.findAndModify(query, updateOperations, findAndModifyOptions);

        });

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + objectId);
        }

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    @Override
    public Profile createOrReactivateProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Insert.class);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.and(
            query.criteria("active").equal(false),
            query.criteria("user").equal(user),
            query.criteria("application").equal(application)
        );

        final UpdateOperations<MongoProfile> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);
        updateOperations.set("user", user);
        updateOperations.set("active", true);
        updateOperations.set("application", application);
        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        updateOperations.set("displayName", nullToEmpty(profile.getDisplayName()).trim());

        if (profile.getMetadata() == null) {
            updateOperations.unset("metadata");
        } else {
            updateOperations.set("metadata", profile.getMetadata());
        }

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
    public Profile createOrReactivateProfile(final Profile profile, final Map<String, Object> metadata) {

        getValidationHelper().validateModel(profile, Insert.class);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.and(
                query.criteria("user").equal(user),
                query.criteria("application").equal(application)
        );

        final UpdateOperations<MongoProfile> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);
        updateOperations.set("user", user);
        updateOperations.set("active", true);
        updateOperations.set("application", application);
        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        updateOperations.set("displayName", nullToEmpty(profile.getDisplayName()).trim());

        if (metadata == null) {
            updateOperations.unset("metadata");
        } else {
            updateOperations.set("metadata", metadata);
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {

            final FindAndModifyOptions findAndModifyOptions = new FindAndModifyOptions()
                    .upsert(true)
                    .returnNew(true);

            return ds.findAndModify(query, updateOperations, findAndModifyOptions);

        });

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    @Override
    public Profile createOrRefreshProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Insert.class);

        final Query<MongoProfile> query = getDatastore().createQuery(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.and(
            query.criteria("user").equal(user),
            query.criteria("application").equal(application)
        );

        final UpdateOperations<MongoProfile> updateOperations;

        updateOperations = getDatastore().createUpdateOperations(MongoProfile.class);

        // We want ot ensuure that the profile is activated and we get a new image URL for the sake of Facebook
        updateOperations.set("active", true);
        updateOperations.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim());

        // These fields are not part of the "refresh" operation, but will get set if it is the first time we create
        // the profile.

        updateOperations.setOnInsert("user", user);
        updateOperations.setOnInsert("application", application);
        updateOperations.setOnInsert("displayName", nullToEmpty(profile.getDisplayName()).trim());

        final MongoProfile mongoProfile;

        mongoProfile = getDatastore().findAndModify(query, updateOperations, new FindAndModifyOptions()
                .upsert(true)
                .returnNew(true));

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    private MongoUser getMongoUserFromProfile(final Profile profile) {
        return getMongoUserDao().getActiveMongoUser(profile.getUser().getId());
    }

    private MongoApplication getMongoApplicationFromProfile(final Profile profile) {
        return getMongoApplicationDao().getActiveMongoApplication(profile.getApplication().getId());
    }

    @Override
    public void softDeleteProfile(String profileId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);
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
