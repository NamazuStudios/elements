package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Pattern;

/**
 * Resolves index-creation conflicts by dropping the conflicting on-disk index and letting
 * {@link MorphiaSelfHealingIndexApplier} re-create the requested one.
 *
 * <p>Two error codes are handled:</p>
 *
 * <ul>
 *     <li>85 ({@code IndexOptionsConflict}): the requested key spec already exists in the collection under a
 *     <b>different name</b>. The server message names the conflicting existing index, bare and unquoted:
 *     {@code Index already exists with a different name: name_1}.</li>
 *     <li>86 ({@code IndexKeySpecsConflict}): the requested name already exists under a different key spec. The
 *     message renders the existing index as a document; the last quoted {@code name: "..."} in the message
 *     describes the on-disk (existing) index.</li>
 * </ul>
 *
 * <p>In both cases the index named by the server is the one blocking creation of the requested index, so it is
 * dropped (ignoring a race where it no longer exists) and the applier retries.</p>
 */
public class MongoIndexConflictResolver implements IndexConflictResolver {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexConflictResolver.class);

    private static final int INDEX_OPTIONS_CONFLICT = 85;

    private static final int INDEX_KEY_SPECS_CONFLICT = 86;

    private static final int INDEX_NOT_FOUND = 27;

    private static final Pattern OPTIONS_CONFLICT_DIFFERENT_NAME = Pattern.compile(
            "Index already exists with a different name:\\s*['\"]?([^'\"\\s;]+)['\"]?"
    );

    private static final Pattern EXISTING_INDEX_NAME = Pattern.compile("name:\\s*\"([^\"]+)\"");

    @Override
    public boolean resolve(final MongoCollection<?> collection, final MongoCommandException cause) {

        final var code = cause.getErrorCode();

        if (code != INDEX_OPTIONS_CONFLICT && code != INDEX_KEY_SPECS_CONFLICT) {
            return false;
        }

        final var indexName = extractIndexName(code, cause.getErrorMessage());

        if (indexName == null) {
            logger.error(
                    "Index conflict on {} could not be resolved; unable to determine conflicting index name from: {}",
                    collection.getNamespace(),
                    cause.getResponse().toJson()
            );
            return false;
        }

        try {
            collection.dropIndex(indexName);
            logger.warn(
                    "Dropped conflicting index {} on {} to resolve {}: {}",
                    indexName,
                    collection.getNamespace(),
                    cause.getErrorCodeName(),
                    cause.getErrorMessage()
            );
        } catch (MongoCommandException dropFailure) {
            if (dropFailure.getErrorCode() != INDEX_NOT_FOUND) {
                throw dropFailure;
            }
        }

        return true;

    }

    /**
     * Determines the name of the on-disk index blocking creation of the requested index.
     *
     * @param code the error code (85 or 86)
     * @param errorMessage the server-provided error message
     * @return the conflicting index name, or {@code null} if it cannot be determined
     */
    private String extractIndexName(final int code, final String errorMessage) {

        if (code == INDEX_OPTIONS_CONFLICT) {
            // Error 85 names the existing index bare, outside of any document, e.g.
            // "Index already exists with a different name: name_1". Prefer this parse, which never
            // confuses the requested index for the existing one.
            final var differentName = OPTIONS_CONFLICT_DIFFERENT_NAME.matcher(errorMessage);
            if (differentName.find()) {
                return differentName.group(1);
            }
        }

        // Fall back to the index document rendering used by error 86 (and by some error 85 variants). The
        // requested index is reported first and the existing index second, so the LAST quoted name in the
        // message is the on-disk index that must be dropped.
        final var existing = EXISTING_INDEX_NAME.matcher(errorMessage);
        String indexName = null;
        while (existing.find()) {
            indexName = existing.group(1);
        }

        return indexName;

    }

}
