package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.DeleteResult;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.TokensWithExpirationDao;
import dev.getelements.elements.dao.mongo.model.MongoTokenWithExpiration;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.token.TokenWithExpiration;
import dev.getelements.elements.sdk.model.user.User;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import org.bson.types.ObjectId;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import jakarta.inject.Inject;
import java.sql.Timestamp;
import java.util.Objects;
import java.util.function.Consumer;

import static dev.morphia.query.filters.Filters.eq;

public class MongoTokensWithExpirationDao implements TokensWithExpirationDao {
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private MapperRegistry dozerMapperRegistry;
    private Consumer<Event> eventPublisher;


    @Override
    public String createToken(TokenWithExpiration token) {
        MongoTokenWithExpiration mongoToken = mapToken(token);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoToken));

        final var createdToken = getDozerMapper().map(mongoToken, TokenWithExpiration.class);

        getEventPublisher().accept(Event.builder()
                .argument(createdToken)
                .named(TOKEN_WITH_EXPIRATION_CREATED)
                .build());

        return mongoToken.getId().toString();
    }

    @Override
    public Timestamp getTokenExpiry(String tokenId) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("_id", new ObjectId(tokenId)));

        return Objects.requireNonNull(query.first()).getExpiry();
    }

    @Override
    public void deleteTokensByUser(User user) {
        final var query = getDatastore().find(MongoTokenWithExpiration.class);

        query.filter(eq("email", user.getEmail()));

        query.delete(new DeleteOptions().multi(true));

        getEventPublisher().accept(Event.builder()
                .argument(user)
                .named(TOKENS_WITH_EXPIRATION_TRUNCATED)
                .build());
    }

    @Override
    public void deleteToken(String tokenId) {
        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);

        final var query = getDatastore().find(MongoTokenWithExpiration.class);
        query.filter(eq("_id", id));

        final var entity = query.first();

        if (entity == null) {
            throw new NotFoundException("Token not found: " + tokenId);
        }

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Token not found: " + tokenId);
        }

        final var deletedToken = getDozerMapper().map(entity, TokenWithExpiration.class);

        getEventPublisher().accept(Event.builder()
                .argument(deletedToken)
                .named(TOKEN_WITH_EXPIRATION_DELETED)
                .build());
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

    public MongoDBUtils getMongoDBUtils() {
        return mongoDBUtils;
    }

    @Inject
    public void setMongoDBUtils(MongoDBUtils mongoDBUtils) {
        this.mongoDBUtils = mongoDBUtils;
    }

    private MongoTokenWithExpiration mapToken(TokenWithExpiration token) {
        return getDozerMapper().map(token, MongoTokenWithExpiration.class);
    }

    public MapperRegistry getDozerMapper() {
        return dozerMapperRegistry;
    }

    @Inject
    public void setDozerMapper(MapperRegistry dozerMapperRegistry) {
        this.dozerMapperRegistry = dozerMapperRegistry;
    }

    public Consumer<Event> getEventPublisher() {
        return eventPublisher;
    }

    @Inject
    public void setEventPublisher(Consumer<Event> eventPublisher) {
        this.eventPublisher = eventPublisher;
    }
}
