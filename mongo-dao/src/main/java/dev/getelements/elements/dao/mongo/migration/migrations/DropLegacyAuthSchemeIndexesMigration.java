package dev.getelements.elements.dao.mongo.migration.migrations;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoDatabase;
import dev.getelements.elements.dao.mongo.migration.PreDatastoreMigration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Drops the pre-sparse {@code oauth2_auth_scheme.name_1} and {@code oidc_auth_scheme.name_1}/{@code issuer_1}
 * indexes, superseded by sparse indexes under new names
 * ({@link dev.getelements.elements.dao.mongo.model.auth.MongoOAuth2AuthScheme},
 * {@link dev.getelements.elements.dao.mongo.model.auth.MongoOidcAuthScheme}).
 *
 * <p>{@code sparse: true} was added to those unique indexes to fix soft-deleted auth schemes colliding on
 * a shared {@code null} value (#56/#58), but since the indexes kept their auto-generated names, any
 * environment that ran before that change already has a non-sparse index under the same name — and
 * MongoDB refuses to redefine an existing index's options under its current name
 * ({@code IndexKeySpecsConflict}), which otherwise crashes {@code Morphia.createDatastore(...)} on every
 * startup (#63). The model classes now declare their sparse indexes under new, explicit names so creation
 * never collides going forward; this migration removes the old, orphaned index so upgraded deployments end
 * up fully clean.
 *
 * NOTE: The catch-all introduced in PR #68 made this migration irrelevant, but I'm leaving it in for now as
 * an example.
 *
 * <p>A no-op if the old index (or the collection itself) is already gone.
 */
public class DropLegacyAuthSchemeIndexesMigration implements PreDatastoreMigration {

    private static final Logger logger = LoggerFactory.getLogger(DropLegacyAuthSchemeIndexesMigration.class);

    public static final String ID = "20260821_01_drop_legacy_auth_scheme_indexes";

    private static final int INDEX_NOT_FOUND = 27;

    private static final int NAMESPACE_NOT_FOUND = 26;

    @Override
    public String getId() {
        return ID;
    }

    @Override
    public String getDescription() {
        return "Drops the pre-sparse oauth2_auth_scheme.name_1 / oidc_auth_scheme.name_1,issuer_1 indexes, " +
                "superseded by sparse indexes under new names (#56/#58, #63).";
    }

    @Override
    public void apply(final MongoDatabase database) {
        dropIfPresent(database, "oauth2_auth_scheme", "name_1");
        dropIfPresent(database, "oidc_auth_scheme", "name_1");
        dropIfPresent(database, "oidc_auth_scheme", "issuer_1");
    }

    private void dropIfPresent(final MongoDatabase database, final String collectionName, final String indexName) {
        try {
            database.getCollection(collectionName).dropIndex(indexName);
            logger.info(
                    "Dropped stale index {} on {} (superseded by a sparse index under a new name).",
                    indexName,
                    collectionName
            );
        } catch (final MongoCommandException ex) {
            if (ex.getErrorCode() != INDEX_NOT_FOUND && ex.getErrorCode() != NAMESPACE_NOT_FOUND) {
                throw ex;
            }
        }
    }

}
