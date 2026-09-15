package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import com.mongodb.client.MongoCollection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MongoIndexConflictResolver implements IndexConflictResolver {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexConflictResolver.class);

    private static final int INDEX_OPTIONS_CONFLICT = 85;

    private static final int INDEX_KEY_SPECS_CONFLICT = 86;

    private static final int INDEX_NOT_FOUND = 27;

    private static final Pattern EXISTING_INDEX_NAME = Pattern.compile("name:\\s*\"([^\"]+)\"");

    @Override
    public boolean resolve(final MongoCollection<?> collection, final MongoCommandException cause) {

        final var code = cause.getErrorCode();

        if (code != INDEX_OPTIONS_CONFLICT && code != INDEX_KEY_SPECS_CONFLICT) {
            return false;
        }

        final var matcher = EXISTING_INDEX_NAME.matcher(cause.getErrorMessage());
        String indexName = null;

        while (matcher.find()) {
            indexName = matcher.group(1);
        }

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

}
