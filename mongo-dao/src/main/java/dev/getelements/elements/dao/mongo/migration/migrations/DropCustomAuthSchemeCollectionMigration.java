package dev.getelements.elements.dao.mongo.migration.migrations;

import dev.getelements.elements.dao.mongo.migration.Migration;
import dev.morphia.Datastore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Drops the legacy {@code auth_scheme} collection created for the custom AuthScheme mechanism.
 * The custom bearer-JWT auth scheme has been removed and is superseded by OIDC/OAuth2 auth schemes.
 *
 * <p>This migration is idempotent: if the collection does not exist, it will be a no-op.
 */
public class DropCustomAuthSchemeCollectionMigration implements Migration {

    private static final Logger logger = LoggerFactory.getLogger(DropCustomAuthSchemeCollectionMigration.class);

    public static final String ID = "20261001_01_drop_custom_auth_scheme_collection";

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Drops the legacy auth_scheme collection from the removed custom AuthScheme mechanism.";
    }

    @Override
    public void apply(final Datastore datastore) {
        final List<String> collectionNames = datastore.getDatabase().listCollectionNames().into(List.of());
        if (collectionNames.contains("auth_scheme")) {
            datastore.getDatabase().getCollection("auth_scheme").drop();
            logger.info("Dropped legacy auth_scheme collection.");
        }
    }
}
