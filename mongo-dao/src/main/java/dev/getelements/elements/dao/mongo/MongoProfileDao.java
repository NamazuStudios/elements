package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.sdk.dao.ProfileDao;
import dev.getelements.elements.dao.mongo.application.MongoApplicationDao;
import dev.getelements.elements.dao.mongo.largeobject.MongoLargeObjectDao;
import dev.getelements.elements.dao.mongo.model.MongoProfile;
import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.application.MongoApplication;
import dev.getelements.elements.dao.mongo.model.largeobject.MongoLargeObject;
import dev.getelements.elements.dao.mongo.query.BooleanQueryParser;
import dev.getelements.elements.sdk.model.exception.BadQueryException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.profile.ProfileNotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.ValidationGroups.Insert;
import dev.getelements.elements.sdk.model.ValidationGroups.Update;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.model.util.ValidationHelper;
import dev.morphia.Datastore;
import dev.morphia.ModifyOptions;
import dev.morphia.UpdateOptions;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import dev.morphia.query.filters.Filters;
import jakarta.inject.Inject;
import org.bson.types.ObjectId;

import java.util.*;
import java.util.stream.Stream;

import static com.google.common.base.Strings.nullToEmpty;
import static com.mongodb.client.model.ReturnDocument.AFTER;
import static dev.morphia.query.filters.Filters.and;
import static dev.morphia.query.filters.Filters.or;
import static dev.morphia.query.filters.Filters.*;
import static dev.morphia.query.updates.UpdateOperators.*;
import static java.lang.String.format;
import static java.util.regex.Pattern.compile;

/**
 *
 * Created by patricktwohig on 6/28/17.
 */
public class MongoProfileDao implements ProfileDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private MapperRegistry beanMapperRegistry;

    private ValidationHelper validationHelper;

    private MongoUserDao mongoUserDao;

    private MongoApplicationDao mongoApplicationDao;

    private MongoLargeObjectDao mongoLargeObjectDao;

    private MongoConcurrentUtils mongoConcurrentUtils;

    private BooleanQueryParser booleanQueryParser;

    @Override
    public Optional<Profile> findActiveProfile(final String profileId) {
        return findActiveMongoProfile(profileId).map(this::transform);
    }

    public Optional<MongoProfile> findActiveMongoProfile(final Profile profile) {
        return profile == null ? Optional.empty() : findActiveMongoProfile(profile.getId());
    }

    public Optional<MongoProfile> findActiveMongoProfile(final String profileId) {
        if (profileId == null || !ObjectId.isValid(profileId)) return Optional.empty();
        final var objectId = new ObjectId(profileId);
        return findActiveMongoProfile(objectId);
    }

    public Optional<MongoProfile> findActiveMongoProfile(final ObjectId profileId) {

        final var query = getDatastore().find(MongoProfile.class);

        query.filter(
                and(
                        eq("_id", profileId),
                        eq("active", true)
                )
        );

        final var mongoProfile = query.first();
        return Optional.ofNullable(mongoProfile);

    }

    @Override
    public Optional<Profile> findActiveProfileForUser(final String profileId, final String userId) {

        if (profileId == null || !ObjectId.isValid(profileId)) return Optional.empty();

        final var objectId = getMongoDBUtils().parseOrReturnNull(profileId);
        final var mongoUser = getMongoUserDao().getMongoUser(userId);

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(eq("_id", objectId));
        query.filter(eq("active", true));
        query.filter(eq("user", mongoUser));

        final MongoProfile mongoProfile = query.first();
        return mongoProfile == null ? Optional.empty() : Optional.of(transform(mongoProfile));

    }

    public Pagination<Profile> getActiveProfiles(
            final int offset,
            final int count,
            final String applicationNameOrId, final String userId,
            final Long lowerBoundTimestamp, final Long upperBoundTimestamp) {
        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(
                eq("active", true)
        );

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

            query.filter(
                    gte("lastLogin", lowerBoundDate)
            );
        }

        if (upperBoundTimestamp != null) {

            final Date upperBoundDate;

            if (upperBoundTimestamp >= 0) {
                upperBoundDate = new Date(upperBoundTimestamp);
            } else {
                upperBoundDate = new Date();
            }

            query.filter(
                    Filters.lte("lastLogin", upperBoundDate)
            );

        }

        if (applicationNameOrId != null) {
            final MongoApplication mongoApplication;
            mongoApplication = getMongoApplicationDao().findActiveMongoApplication(applicationNameOrId);
            if (mongoApplication == null) return new Pagination<>();
            query.filter(
                    eq("application", mongoApplication)
            );
        }

        if (userId != null) {
            final Optional<MongoUser> mongoUser = getMongoUserDao().findMongoUser(userId);
            if (mongoUser.isEmpty()) return new Pagination<>();
            query.filter(eq("user", mongoUser.get()));
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

        final var query = getBooleanQueryParser()
                .parse(MongoProfile.class, trimmedSearch)
                .orElseGet(() -> parseLegacyQuery(trimmedSearch));

        return getMongoDBUtils().isIndexedQuery(query)
                ? paginationFromQuery(query, offset, count)
                : Pagination.empty();

    }

    public Query<MongoProfile> parseLegacyQuery(final String search) {

        final String trimmedSearch = nullToEmpty(search).trim();

        if (trimmedSearch.isEmpty()) {
            throw new BadQueryException("search must be specified.");
        }

        final Query<MongoProfile> profileQuery = getDatastore().find(MongoProfile.class);
        final Query<MongoUser> userQuery = getDatastore().find(MongoUser.class);

        userQuery.filter(
                or(
                        regex("name", compile(trimmedSearch)),
                        regex("email", compile(trimmedSearch))
                )
        );

        final List<MongoUser> userList;

        try (var iterator = userQuery.iterator()) {
            userList = iterator.toList();
        }

        return profileQuery.filter(
                eq("active", true),
                or(
                        regex("displayName", compile(trimmedSearch)),
                        in("user", userList)
                )
        );

    }

    private Pagination<Profile> paginationFromQuery(final Query<MongoProfile> query, final int offset, final int count) {
        return getMongoDBUtils().paginationFromQuery(query, offset, count, u -> getBeanMapper().map(u, Profile.class), new FindOptions());
    }

    @Override
    public Profile getActiveProfile(final String profileId) {
        final MongoProfile mongoProfile = getActiveMongoProfile(profileId);
        return getBeanMapper().map(mongoProfile, Profile.class);
    }

    public MongoProfile getActiveMongoProfile(final Profile profile) {
        return getActiveMongoProfile(profile.getId());
    }

    public MongoProfile getActiveMongoProfile(final String profileId) {
        final ObjectId objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);
        return getActiveMongoProfile(objectId);
    }

    public MongoProfile getActiveMongoProfile(final ObjectId objectId) {
        return findActiveMongoProfile(objectId).orElseThrow(() -> {
            return new ProfileNotFoundException("No profile exists with id " + objectId);
        });
    }

    public Stream<MongoProfile> getActiveMongoProfilesForUser(final MongoUser user) {

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(and(
                eq("user", user),
                eq("active", true)
        ));

        try (var iterator = query.iterator()) {
            return iterator.toList().stream();
        }

    }

    public Stream<MongoProfile> getActiveMongoProfilesForUser(final ObjectId mongoUserObjectId) {
        final MongoUser mongoUser = new MongoUser();
        mongoUser.setObjectId(mongoUserObjectId);
        return getActiveMongoProfilesForUser(mongoUser);
    }

    @Override
    public Profile updateActiveProfile(final Profile profile, final Map<String, Object> metadata) {

        getValidationHelper().validateModel(profile, Update.class);

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(profile.getId());
        final var query = getDatastore().find(MongoProfile.class);
        final var imageObject = getMongoLargeObjectFromProfile(profile);

        final var builder = new UpdateBuilder();

        query.filter(and(
            eq("active", true),
            eq("_id", objectId)
        ));

        builder.with(
            set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
            set("displayName", nullToEmpty(profile.getDisplayName()).trim())
        );

        if (imageObject != null) {
            builder.with(set("imageObject", imageObject));
        }

        if (metadata == null) {
            builder.with(unset("metadata"));
        } else {
            builder.with(set("metadata", metadata));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profile.getId());
        }

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

        final var query = getDatastore().find(MongoProfile.class);

        query.filter(and(
            eq("active", true),
            eq("_id", objectId)
        ));

        final var builder = new UpdateBuilder();

        if (metadata == null) {
            builder.with(unset("metadata"));
        } else {
            builder.with(set("metadata", metadata));
        }

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false).returnDocument(AFTER))
        );

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + objectId);
        }

        return transform(mongoProfile);

    }

    @Override
    public Profile createOrReactivateProfile(final Profile profile, final Map<String, Object> metadata) {

        getValidationHelper().validateModel(profile, Insert.class);

        final var query = getDatastore().find(MongoProfile.class);
        final var user = getMongoUserFromProfile(profile);
        final var application = getMongoApplicationFromProfile(profile);
        final var imageObject = getMongoLargeObjectFromProfile(profile);

        query.filter(and(
            eq("user", user),
            eq("active", false),
            eq("application", application)
        ));

        final var builder = new UpdateBuilder().with(
            set("user", user),
            set("active", true),
            set("application", application),
            set("imageUrl", nullToEmpty(profile.getImageUrl()).trim()),
            set("displayName", nullToEmpty(profile.getDisplayName()).trim())
        );

        if (imageObject != null) {
            builder.with(set("imageObject", imageObject));
        }

        if (metadata == null) {
            builder.with(unset("metadata"));
        } else {
            builder.with(set("metadata", profile.getMetadata()));
        }

        final var mongoProfile = getMongoDBUtils().perform(
            ds -> builder.execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        if (mongoProfile == null) {
            throw new ProfileNotFoundException("profile not found: " + profile.getId());
        }

        return transform(mongoProfile);

    }

    @Override
    public Profile createOrRefreshProfile(final Profile profile) {

        getValidationHelper().validateModel(profile, Insert.class);

        final var query = getDatastore().find(MongoProfile.class);

        final var user = getMongoUserFromProfile(profile);
        final var application = getMongoApplicationFromProfile(profile);

        query.filter(and(
            eq("user", user),
            eq("application", application)
        ));

        // These fields are not part of the "refresh" operation, but will get set if it is the first time we create
        // the profile.
        final var insertMap = new HashMap<String, Object>(Collections.emptyMap());

        insertMap.put("imageUrl", nullToEmpty(profile.getImageUrl()).trim());
        insertMap.put("displayName", nullToEmpty(profile.getDisplayName()).trim());

        final var builder = new UpdateBuilder();

        final var mongoProfile = getMongoDBUtils().perform(ds ->
            builder.with(
                set("active", true),
                set("user", user),
                set("application", application),
                setOnInsert(insertMap)
            ).execute(query, new ModifyOptions().upsert(true).returnDocument(AFTER))
        );

        return transform(mongoProfile);

    }

    private MongoUser getMongoUserFromProfile(final Profile profile) {
        return getMongoUserDao().getMongoUser(profile.getUser().getId());
    }

    private MongoApplication getMongoApplicationFromProfile(final Profile profile) {
        return getMongoApplicationDao().getActiveMongoApplication(profile.getApplication().getId());
    }

    private MongoLargeObject getMongoLargeObjectFromProfile(final Profile profile) {
        return  profile.getImageObject() == null ? null :
                getMongoLargeObjectDao().findMongoLargeObject(profile.getImageObject().getId())
                        .orElseThrow(()-> new NotFoundException(format("Not found Large object %s asociated with profile %s",
                                profile.getImageObject().getId(), profile.getId())));
    }

    @Override
    public void softDeleteProfile(String profileId) {

        final var objectId = getMongoDBUtils().parseOrThrowNotFoundException(profileId);
        final var query = getDatastore().find(MongoProfile.class);

        query.filter(and(
            eq("active", true),
            eq("_id", objectId)
        ));

        final var builder = new UpdateBuilder().with(set("active", false));

        final MongoProfile mongoProfile = getMongoDBUtils().perform(ds ->
            builder.execute(query, new ModifyOptions().upsert(false))
        );

        if (mongoProfile == null) {
            throw new NotFoundException("application not found: " + profileId);
        }

    }

    public void softDeleteProfilesForUser(MongoUser mongoUser) {

        final Query<MongoProfile> query = getDatastore().find(MongoProfile.class);

        query.filter(and(
                eq("user", mongoUser),
                eq("active", true)
        ));

        final var builder = new UpdateBuilder().with(set("active", false));

        getMongoDBUtils().perform(ds ->
                builder.execute(query, new UpdateOptions().upsert(false).multi(true))
        );

    }

    public Profile transform(final MongoProfile input) {

        if (input.getApplication().getName() == null) {
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

    public MapperRegistry getBeanMapper() {
        return beanMapperRegistry;
    }

    @Inject
    public void setBeanMapper(MapperRegistry beanMapperRegistry) {
        this.beanMapperRegistry = beanMapperRegistry;
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

    public MongoConcurrentUtils getMongoConcurrentUtils() {
        return mongoConcurrentUtils;
    }

    @Inject
    public void setMongoConcurrentUtils(MongoConcurrentUtils mongoConcurrentUtils) {
        this.mongoConcurrentUtils = mongoConcurrentUtils;
    }

    public MongoLargeObjectDao getMongoLargeObjectDao() {
        return mongoLargeObjectDao;
    }

    @Inject
    public void setMongoLargeObjectDao(MongoLargeObjectDao mongoLargeObjectDao) {
        this.mongoLargeObjectDao = mongoLargeObjectDao;
    }

    public BooleanQueryParser getBooleanQueryParser() {
        return booleanQueryParser;
    }

    @Inject
    public void setBooleanQueryParser(BooleanQueryParser booleanQueryParser) {
        this.booleanQueryParser = booleanQueryParser;
    }

}
