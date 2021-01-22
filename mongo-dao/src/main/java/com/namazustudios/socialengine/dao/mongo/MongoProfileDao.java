package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.elements.fts.ObjectIndex;
import com.namazustudios.socialengine.dao.ProfileDao;
import com.namazustudios.socialengine.dao.mongo.application.MongoApplicationDao;
import com.namazustudios.socialengine.dao.mongo.model.MongoProfile;
import com.namazustudios.socialengine.dao.mongo.model.MongoUser;
import com.namazustudios.socialengine.dao.mongo.model.application.MongoApplication;
import com.namazustudios.socialengine.exception.BadQueryException;
import com.namazustudios.socialengine.exception.InvalidDataException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.exception.profile.ProfileNotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.ValidationGroups.Insert;
import com.namazustudios.socialengine.model.ValidationGroups.Update;
import com.namazustudios.socialengine.model.profile.Profile;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.util.ValidationHelper;
import dev.morphia.UpdateOptions;
import dev.morphia.query.experimental.filters.Filters;
import dev.morphia.query.experimental.updates.UpdateOperators;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.index.Term;
import org.apache.lucene.queryparser.flexible.core.QueryNodeException;
import org.apache.lucene.queryparser.flexible.standard.StandardQueryParser;
import org.apache.lucene.search.*;
import org.bson.types.ObjectId;
import org.dozer.Mapper;
import dev.morphia.Datastore;
import dev.morphia.FindAndModifyOptions;
import dev.morphia.query.*;
import dev.morphia.query.Query;

import javax.inject.Inject;
import java.util.*;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static com.google.common.base.Strings.nullToEmpty;

/**
 *
 * Created by patricktwohig on 6/28/17.
 */
public class MongoProfileDao implements ProfileDao {

    private ObjectIndex objectIndex;

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private Mapper beanMapper;

    private StandardQueryParser standardQueryParser;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    @Override
    public Optional<Profile> findActiveProfile(final String profileId) {

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);
        if (!ObjectId.isValid(profileId)) throw new ProfileNotFoundException();

        query.filter(Filters.and(
                Filters.eq("_id", new ObjectId(profileId)),
                Filters.eq("active", true)
                ));

        final MongoProfile mongoProfile = query.first();
        return mongoProfile == null ? Optional.empty() : Optional.of(transform(mongoProfile));

    }

    @Override
    public Optional<Profile> findActiveProfileForUser(final String profileId, final String userId) {

        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);
        final MongoUser mongoUser = getMongoUserDao().getActiveMongoUser(userId);

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.eq("_id", objectId));
        query.filter(Filters.eq("active", true));
        query.filter(Filters.eq("user", mongoUser));

        final MongoProfile mongoProfile = query.first();
        return mongoProfile == null ? Optional.empty() : Optional.of(transform(mongoProfile));

    }

    public Pagination<Profile> getActiveProfiles(
            final int offset,
            final int count,
            final String applicationNameOrId, final String userId,
            final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("active", true)
        ));

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

            query.filter(Filters.and(
                    Filters.gte("lastLogin", lowerBoundDate)
            ));
        }

        if (upperBoundTimestamp != null) {

            final Date upperBoundDate;

            if (upperBoundTimestamp >= 0) {
                upperBoundDate = new Date(upperBoundTimestamp);
            } else {
                upperBoundDate = new Date();
            }

            query.filter(Filters.and(
                    Filters.lte("lastLogin", upperBoundDate)
            ));

        }

        if (applicationNameOrId != null) {
            final MongoApplication mongoApplication;
            mongoApplication = getMongoApplicationDao().findActiveMongoApplication(applicationNameOrId);
            if (mongoApplication == null) return new Pagination<>();
            query.filter(Filters.and(
                    Filters.eq("application", mongoApplication)
            ));
        }

        if (userId != null) {
            final MongoUser mongoUser = getMongoUserDao().findActiveMongoUser(userId);
            if (mongoUser == null) return new Pagination<>();
            query.filter(Filters.and(
                    Filters.eq("user", mongoUser)
            ));
        }

        return getMongoDBUtils().paginationFromQuery(query, offset, count, input -> transform(input), new FindOptions());

    }

    @Override
    public Pagination<Profile> getActiveProfiles(
            final int offset,
            final int count,
            final String search) {

        final String trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            throw new InvalidDataException("search must be specified.");
        }

        final Query<MongoProfile> profileQuery = getDatastore().find(MongoProfile.class);
        final Query<MongoUser> userQuery = getDatastore().find(MongoUser.class);

        userQuery.filter(
                Filters.eq("active", true),
                Filters.or(
                        Filters.regex("name").pattern(Pattern.compile(trimmedSearch)),
                        Filters.regex("email").pattern(Pattern.compile(trimmedSearch))
                )
        );

        profileQuery.filter(
                Filters.eq("active", true),
                Filters.or(
                        Filters.regex("displayName").pattern(Pattern.compile(trimmedSearch)),
                        Filters.in("user", userQuery.iterator().toList())
                )
        );

        return paginationFromQuery(profileQuery, offset, count);
    }

    private Pagination<Profile> paginationFromQuery(final Query<MongoProfile> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getBeanMapper().map(u, Profile.class), new FindOptions());
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

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("_id", objectId)
        ));

        final MongoProfile mongoProfile = query.first();

        if (mongoProfile == null) {
            throw new ProfileNotFoundException("No profile exists with id " + profileId);
        }

        return mongoProfile;
    }

    public Stream<MongoProfile> getActiveMongoProfilesForUser(final MongoUser user) {

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("user", user),
                Filters.eq("active", true)
        ));

        return query.iterator().toList().stream();

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
        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("_id", objectId)
        ));

        query.update(UpdateOperators.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
                UpdateOperators.set("displayName", nullToEmpty(profile.getDisplayName()).trim()))
                .execute(new UpdateOptions().upsert(false));

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return ds.find(MongoProfile.class).filter(Filters.eq("_id", objectId)).first();
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
        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("_id", objectId)
        ));

        query.update(UpdateOperators.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
                UpdateOperators.set("displayName", nullToEmpty(profile.getDisplayName()).trim())
                ).execute(new UpdateOptions().upsert(false));

        if (metadata == null) {
            query.update(UpdateOperators.unset("metadata"))
                    .execute(new UpdateOptions().upsert(false));
        } else {
            query.update(UpdateOperators.set("metadata", metadata))
                    .execute(new UpdateOptions().upsert(false));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return ds.find(MongoProfile.class).filter(Filters.eq("_id", objectId)).first();
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

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("_id", objectId)
        ));

        if (metadata == null) {
            query.update(UpdateOperators.unset("metadata"))
                    .execute(new UpdateOptions().upsert(false));
        } else {
            query.update(UpdateOperators.set("metadata", metadata))
                    .execute(new UpdateOptions().upsert(false));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return ds.find(MongoProfile.class).filter(Filters.eq("_id", objectId)).first();
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

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("user", user),
                Filters.eq("application", application)
        ));

        query.update(UpdateOperators.set("user", user),
                UpdateOperators.set("active", true),
                UpdateOperators.set("application", application),
                UpdateOperators.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
                UpdateOperators.set("displayName", nullToEmpty(profile.getDisplayName()).trim())
                ).execute(new UpdateOptions().upsert(true));

        if (profile.getMetadata() == null) {
            query.update(UpdateOperators.unset("metadata"))
                    .execute(new UpdateOptions().upsert(true));
        } else {
            query.update(UpdateOperators.set("metadata", profile.getMetadata()))
                    .execute(new UpdateOptions().upsert(true));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return query.first();
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

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.filter(Filters.and(
                Filters.eq("user", user),
                Filters.eq("application", application)
        ));

        query.update(UpdateOperators.set("user", user),
                UpdateOperators.set("active", true),
                UpdateOperators.set("application", application),
                UpdateOperators.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
                UpdateOperators.set("displayName", nullToEmpty(profile.getDisplayName()).trim())
        ).execute(new UpdateOptions().upsert(true));

        if (metadata == null) {
            query.update(UpdateOperators.unset("metadata"))
                    .execute(new UpdateOptions().upsert(true));
        } else {
            query.update(UpdateOperators.set("metadata", metadata))
                    .execute(new UpdateOptions().upsert(true));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return query.first();
        });

        getObjectIndex().index(mongoProfile);
        return transform(mongoProfile);

    }

    @Override
    public Profile createOrRefreshProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Insert.class);

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        final MongoUser user = getMongoUserFromProfile(profile);
        final MongoApplication application = getMongoApplicationFromProfile(profile);

        query.filter(Filters.and(
                Filters.eq("user", user),
                Filters.eq("application", application)
        ));


        // These fields are not part of the "refresh" operation, but will get set if it is the first time we create
        // the profile.
        final Map<String, Object> insertMap = new HashMap<>(Collections.emptyMap());

        insertMap.put("user", user);
        insertMap.put("application", application);
        insertMap.put("displayName", nullToEmpty(profile.getDisplayName()).trim());

        query.update(UpdateOperators.set("active", true),
                UpdateOperators.set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
                UpdateOperators.setOnInsert(insertMap)
        ).execute(new UpdateOptions().upsert(true));

        final MongoProfile mongoProfile;

        mongoProfile = query.first();

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
        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(Filters.and(
                Filters.eq("active", true),
                Filters.eq("_id", objectId)
        ));

        query.update(UpdateOperators.set("active", false)).execute(new UpdateOptions().upsert(false));

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds -> {
            return query.first();
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

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
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
