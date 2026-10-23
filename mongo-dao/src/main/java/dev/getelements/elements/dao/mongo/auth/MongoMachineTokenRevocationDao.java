package dev.getelements.elements.dao.mongo.auth;

import dev.getelements.elements.dao.mongo.model.auth.MongoMachineTokenRevocation;
import dev.getelements.elements.sdk.dao.MachineTokenRevocationDao;
import dev.morphia.Datastore;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;

import java.sql.Timestamp;

import static dev.morphia.query.filters.Filters.eq;
import static java.lang.System.currentTimeMillis;

/**
 * MongoDB implementation of {@link MachineTokenRevocationDao}. The {@code _id} is the token's {@code jti},
 * and the expiring-collection read defensively re-checks expiry because Mongo's TTL sweep is asynchronous.
 */
@Singleton
public class MongoMachineTokenRevocationDao implements MachineTokenRevocationDao {

    private Datastore datastore;

    @Override
    public void revoke(final String tokenId, final Timestamp expiry) {
        final var entity = new MongoMachineTokenRevocation();
        entity.setId(tokenId);
        entity.setExpiry(expiry);
        getDatastore().save(entity);
    }

    @Override
    public boolean isRevoked(final String tokenId) {

        final var entity = getDatastore().find(MongoMachineTokenRevocation.class)
                .filter(eq("_id", tokenId))
                .first();

        return entity != null
                && entity.getExpiry() != null
                && entity.getExpiry().after(new Timestamp(currentTimeMillis()));

    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
