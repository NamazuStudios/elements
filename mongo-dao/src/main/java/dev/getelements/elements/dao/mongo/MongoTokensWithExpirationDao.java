package dev.getelements.elements.dao.mongo;

import com.mongodb.client.result.DeleteResult;
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

import static dev.morphia.query.filters.Filters.eq;

public class MongoTokensWithExpirationDao implements TokensWithExpirationDao {
    private MongoDBUtils mongoDBUtils;
    private Datastore datastore;
    private MapperRegistry dozerMapperRegistry;


    @Override
    public String createToken(TokenWithExpiration token) {
        MongoTokenWithExpiration mongoToken = mapToken(token);
        getMongoDBUtils().performV(ds -> getDatastore().insert(mongoToken));

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
    }

    @Override
    public void deleteToken(String tokenId) {
        final ObjectId id = getMongoDBUtils().parseOrThrowNotFoundException(tokenId);

        final var query = getDatastore().find(MongoTokenWithExpiration.class);
        query.filter(eq("_id", id));

        final DeleteResult deleteResult = query.delete();

        if (deleteResult.getDeletedCount() == 0) {
            throw new NotFoundException("Token not found: " + tokenId);
        }
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
}
