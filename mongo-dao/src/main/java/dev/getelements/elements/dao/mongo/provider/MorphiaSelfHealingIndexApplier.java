package dev.getelements.elements.dao.mongo.provider;

import com.mongodb.MongoCommandException;
import dev.morphia.Datastore;
import dev.morphia.annotations.internal.IndexHelper;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MorphiaSelfHealingIndexApplier implements SelfHealingIndexApplier {

    private static final Logger logger = LoggerFactory.getLogger(MorphiaSelfHealingIndexApplier.class);

    private static final int MAX_ATTEMPTS = 8;

    @Inject
    private IndexConflictResolver indexConflictResolver;

    @Override
    public void applyIndexes(final Datastore datastore) {

        final var mapper = datastore.getMapper();
        final var indexHelper = new IndexHelper(mapper);

        for (final var model : mapper.getMappedEntities()) {

            if (model.getIdProperty() == null) {
                continue;
            }

            final var collection = datastore.getCollection(model.getType());

            for (int attempt = 1; ; attempt++) {
                try {
                    indexHelper.createIndex(collection, model);
                    break;
                } catch (MongoCommandException e) {

                    if (attempt >= MAX_ATTEMPTS || !indexConflictResolver.resolve(collection, e)) {
                        logger.error(
                                "Unable to self-heal index conflict for {} on {} after {} attempt(s)",
                                model.getType().getName(),
                                collection.getNamespace(),
                                attempt
                        );
                        throw e;
                    }

                }
            }

        }

    }

}
