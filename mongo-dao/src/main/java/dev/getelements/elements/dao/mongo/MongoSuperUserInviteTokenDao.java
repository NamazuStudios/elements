package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.mongo.model.MongoUser;
import dev.getelements.elements.dao.mongo.model.invite.MongoSuperUserInviteToken;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.SuperUserInviteTokenDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.invite.SuperUserInvite;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.query.FindOptions;
import dev.morphia.query.Query;
import jakarta.inject.Inject;

import java.sql.Timestamp;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

import static dev.morphia.query.Sort.descending;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.gt;

public class MongoSuperUserInviteTokenDao implements SuperUserInviteTokenDao {

    private Datastore datastore;

    private MongoUserDao mongoUserDao;

    private MapperRegistry mapperRegistry;

    private Consumer<Event> eventPublisher;

    @Override
    public String createInvite(final User invitedBy, final String invitedEmail, final Timestamp expiry, final Timestamp createdAt) {

        final var token = UUID.randomUUID().toString();
        final var mongoUser = invitedBy == null ? null : getMongoUserDao().getMongoUser(invitedBy.getId());

        final var entity = new MongoSuperUserInviteToken();
        entity.setToken(token);
        entity.setInvitedEmail(invitedEmail == null ? null : invitedEmail.trim().toLowerCase());
        entity.setInvitedBy(mongoUser);
        entity.setExpiry(expiry);
        entity.setCreatedAt(createdAt);
        entity.setStatus(SuperUserInvite.Status.PENDING.name());

        getDatastore().insert(entity);

        final var createdInvite = getMapperRegistry().map(entity, SuperUserInvite.class);

        getEventPublisher().accept(Event.builder()
                .argument(createdInvite)
                .named(SUPERUSER_INVITE_CREATED)
                .build());

        return token;
    }

    @Override
    public Optional<SuperUserInvite> findToken(final String token) {
        return findTokenAnyState(token).filter(this::isRedeemable);
    }

    @Override
    public Optional<SuperUserInvite> findTokenAnyState(final String token) {

        if (token == null || token.isBlank()) {
            return Optional.empty();
        }

        final var entity = getDatastore().find(MongoSuperUserInviteToken.class)
                .filter(eq("_id", token))
                .first();

        if (entity == null) {
            return Optional.empty();
        }

        return Optional.of(getMapperRegistry().map(entity, SuperUserInvite.class));
    }

    @Override
    public Optional<SuperUserInvite> findPendingInviteByEmail(final String email) {

        if (email == null || email.isBlank()) {
            return Optional.empty();
        }

        final var normalized = email.trim().toLowerCase();
        final var now = new Timestamp(System.currentTimeMillis());

        final var query = getDatastore().find(MongoSuperUserInviteToken.class)
                .filter(
                        eq("invitedEmail", normalized),
                        eq("status", SuperUserInvite.Status.PENDING.name()),
                        gt("expiry", now));

        try (var iterator = query.iterator(new FindOptions().sort(descending("createdAt")).limit(1))) {

            final var entity = iterator.tryNext();

            if (entity == null) {
                return Optional.empty();
            }

            return Optional.of(getMapperRegistry().map(entity, SuperUserInvite.class));

        }
    }

    @Override
    public Pagination<SuperUserInvite> getInvites(final int offset, final int count) {

        final Query<MongoSuperUserInviteToken> query = getDatastore().find(MongoSuperUserInviteToken.class);

        final var options = new FindOptions().sort(descending("createdAt"));

        final var pagination = new Pagination<SuperUserInvite>();
        pagination.setOffset(offset);
        pagination.setTotal((int) query.count());

        final var entities = query.iterator(options.skip(offset).limit(count)).toList();

        pagination.setObjects(entities.stream()
                .map(e -> getMapperRegistry().map(e, SuperUserInvite.class))
                .toList());

        return pagination;
    }

    @Override
    public boolean redeemToken(final String token, final Timestamp redeemedAt) {
        return transitionPending(token, SuperUserInvite.Status.REDEEMED, redeemedAt, SUPERUSER_INVITE_REDEEMED);
    }

    @Override
    public boolean revokeToken(final String token, final Timestamp revokedAt) {
        return transitionPending(token, SuperUserInvite.Status.REVOKED, revokedAt, SUPERUSER_INVITE_REVOKED);
    }

    private boolean transitionPending(final String token,
                                      final SuperUserInvite.Status targetStatus,
                                      final Timestamp at,
                                      final String eventName) {

        if (token == null || token.isBlank()) {
            return false;
        }

        final var entity = getDatastore().find(MongoSuperUserInviteToken.class)
                .filter(eq("_id", token))
                .first();

        if (entity == null || !SuperUserInvite.Status.PENDING.name().equals(entity.getStatus())) {
            return false;
        }

        final var now = new Timestamp(System.currentTimeMillis());

        if (entity.getExpiry() != null && entity.getExpiry().before(now)) {
            return false;
        }

        entity.setStatus(targetStatus.name());

        if (targetStatus == SuperUserInvite.Status.REDEEMED) {
            entity.setRedeemedAt(at);
        } else {
            entity.setRevokedAt(at);
        }

        getDatastore().merge(entity);

        final var invite = getMapperRegistry().map(entity, SuperUserInvite.class);

        getEventPublisher().accept(Event.builder()
                .argument(invite)
                .named(eventName)
                .build());

        return true;
    }

    private boolean isRedeemable(final SuperUserInvite invite) {
        final var now = new Timestamp(System.currentTimeMillis());
        return SuperUserInvite.Status.PENDING.equals(invite.getStatus())
                && (invite.getExpiry() == null || invite.getExpiry().after(now));
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoUserDao getMongoUserDao() {
        return mongoUserDao;
    }

    @Inject
    public void setMongoUserDao(MongoUserDao mongoUserDao) {
        this.mongoUserDao = mongoUserDao;
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }

}
