package dev.getelements.elements.dao.mongo.auth;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.dao.mongo.MongoDBUtils;
import dev.getelements.elements.dao.mongo.model.auth.MongoJwtSigningKey;
import dev.getelements.elements.sdk.dao.JwtSigningKeyDao;
import dev.getelements.elements.sdk.model.auth.JwtSigningKey;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.morphia.Datastore;
import jakarta.inject.Inject;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static dev.morphia.query.filters.Filters.eq;

public class MongoJwtSigningKeyDao implements JwtSigningKeyDao {

    private MongoDBUtils mongoDBUtils;

    private Datastore datastore;

    private ObjectMapper objectMapper;

    @Override
    public List<JwtSigningKey> getSigningKeys() {
        try (final var stream = getDatastore().find(MongoJwtSigningKey.class).stream()) {
            return stream
                    .map(this::transform)
                    .sorted(Comparator.comparingLong(JwtSigningKey::getCreatedAt).reversed())
                    .toList();
        }
    }

    @Override
    public List<JwtSigningKey> getSigningKeys(final JwtSigningKey.Status status) {
        try (final var stream = getDatastore()
                .find(MongoJwtSigningKey.class)
                .filter(eq("status", status))
                .stream()) {
            return stream
                    .map(this::transform)
                    .sorted(Comparator.comparingLong(JwtSigningKey::getCreatedAt).reversed())
                    .toList();
        }
    }

    @Override
    public Optional<JwtSigningKey> findSigningKey(final String kid) {
        final var mongoKey = getDatastore()
                .find(MongoJwtSigningKey.class)
                .filter(eq("_id", kid))
                .first();
        return Optional.ofNullable(mongoKey).map(this::transform);
    }

    @Override
    public JwtSigningKey createSigningKey(final JwtSigningKey jwtSigningKey) {

        if (findSigningKey(jwtSigningKey.getKid()).isPresent()) {
            throw new IllegalStateException("Signing key already exists: " + jwtSigningKey.getKid());
        }

        final var entity = transformToMongo(jwtSigningKey);
        final var saved = getMongoDBUtils().perform(ds -> getDatastore().save(entity));
        return transform(saved);

    }

    @Override
    public JwtSigningKey updateSigningKey(final JwtSigningKey jwtSigningKey) {
        final var entity = transformToMongo(jwtSigningKey);
        final var saved = getMongoDBUtils().perform(ds -> getDatastore().merge(entity));
        return transform(saved);
    }

    private JwtSigningKey transform(final MongoJwtSigningKey mongoKey) {

        final var key = new JwtSigningKey();
        key.setKid(mongoKey.getKid());
        key.setAlgorithm(mongoKey.getAlgorithm());
        key.setStatus(mongoKey.getStatus());
        key.setPublicKey(mongoKey.getPublicKey());
        key.setPrivateKey(mongoKey.getPrivateKey());
        key.setCreatedAt(mongoKey.getCreatedAt());

        if (mongoKey.getEncryption() != null) {
            key.setEncryption(getObjectMapper().convertValue(mongoKey.getEncryption(), JwtSigningKey.Encryption.class));
        }

        return key;

    }

    private MongoJwtSigningKey transformToMongo(final JwtSigningKey key) {

        final var mongoKey = new MongoJwtSigningKey();
        mongoKey.setKid(key.getKid());
        mongoKey.setAlgorithm(key.getAlgorithm());
        mongoKey.setStatus(key.getStatus());
        mongoKey.setPublicKey(key.getPublicKey());
        mongoKey.setPrivateKey(key.getPrivateKey());
        mongoKey.setCreatedAt(key.getCreatedAt());

        if (key.getEncryption() != null) {
            final Map<String, Object> encryption = getObjectMapper().convertValue(key.getEncryption(), Map.class);
            mongoKey.setEncryption(encryption);
        }

        return mongoKey;

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

    public ObjectMapper getObjectMapper() {
        return objectMapper;
    }

    @Inject
    public void setObjectMapper(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

}
