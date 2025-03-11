package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.MongoCollection;
import dev.getelements.elements.sdk.dao.Indexable;
import dev.getelements.elements.sdk.dao.index.IndexPlanner;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexMetadata;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlan;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlanStep;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.index.IndexMetadata;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.rt.exception.InternalException;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import org.bson.Document;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import jakarta.inject.Inject;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.sdk.model.index.IndexPlanState.APPLIED;
import static dev.getelements.elements.sdk.model.index.IndexPlanState.READY;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public class MongoDistinctInventoryItemIndexable implements Indexable {

    private static final Logger logger = LoggerFactory.getLogger(MongoDistinctInventoryItem.class);

    private MapperRegistry mapperRegistry;

    private Datastore datastore;

    @Override
    public void plan() {

        final var collection = getDatastore()
                .getMapper()
                .getEntityModel(MongoDistinctInventoryItem.class)
                .getCollectionName();

        final var existing = getDatastore().find(MongoIndexPlan.class)
                .filter(eq("_id", collection))
                .stream()
                .findFirst();

        if (existing.isPresent()) {
            switch (existing.get().getState()) {
                case READY:
                    logger.info("Plan in ready state. No processing needed.");
                    return;
                case APPLIED:
                    logger.info("Previous plan applied. Refreshing with new plan.");
                    break;
                case PROCESSING:
                    logger.warn("Plan currently processing.");
                    return;
                default:
                    throw new InternalException("Unexpected plan state: " + existing.get().getState());
            }
        }

        final var plan = existing
                .map(p -> new IndexPlanner.Builder<Document>()
                        .withExisting(p.getExisting())
                        .build(this::generate)
                )
                .orElseGet(() -> new IndexPlanner.Builder<Document>()
                        .build(this::generate)
                );

        getDatastore().find(MongoItem.class)
                .filter(eq("category", DISTINCT), exists("metadataSpec"))
                .stream()
                .forEach(item -> {
                    final var spec = getMapper().map(item.getMetadataSpec(), MetadataSpec.class);
                    plan.update(item.getName(), spec);
                });

        final var steps = plan.getFinalExecutionSteps()
                        .stream()
                        .map(step -> getMapper().map(step, MongoIndexPlanStep.class))
                        .collect(toList());

        final var options = new UpdateOptions().upsert(existing.isEmpty());

        existing
                .map(p -> getDatastore().queryByExample(p))
                .orElseGet(() -> getDatastore().find(MongoIndexPlan.class).filter(eq("_id", collection)))
                .update(options, set("steps", steps), set("state", READY));

    }

    @Override
    public void buildIndexes() {

        final var collection = getDatastore()
                .getCollection(MongoDistinctInventoryItem.class);

        final var collectionName = getDatastore()
                .getMapper()
                .getEntityModel(MongoDistinctInventoryItem.class)
                .getCollectionName();

        final var plan = getDatastore().find(MongoIndexPlan.class)
                .filter(eq("_id", collectionName))
                .stream()
                .findFirst()
                .orElseThrow(() -> new NotFoundException("No index plan."));

        for(var step : plan.getSteps()) {
            switch (step.getOperation()) {
                case CREATE:
                    create(collection, step);
                    break;
                case REPLACE:
                    delete(collection, step);
                    create(collection, step);
                case LEAVE_AS_IS:
                    logger.info("Leaving {} as-is.", step.getIndexMetadata().getIdentifier());
                    break;
                case DELETE:
                    delete(collection, step);
                    break;
                default:
                    break;
            }
        }

        final var existing = plan.getSteps()
                .stream()
                .map(MongoIndexPlanStep::getIndexMetadata)
                .collect(toList());

        final var options = new UpdateOptions();

        getDatastore().find(MongoIndexPlan.class)
                .filter(eq("_id", collectionName))
                .update(options, set("existing", existing), set("state", APPLIED));

    }

    private void create(
            final MongoCollection<MongoDistinctInventoryItem> collection,
            final MongoIndexPlanStep step) {
        logger.info("Creating {}", step.getIndexMetadata().getIdentifier());
        collection.createIndex(step.getIndexMetadata().getKeys());
    }

    private void delete(
            final MongoCollection<MongoDistinctInventoryItem> collection,
            final MongoIndexPlanStep step) {
        logger.info("Deleting {}", step.getIndexMetadata().getIdentifier());
        collection.dropIndex(step.getIndexMetadata().getKeys());
    }

    private IndexMetadata<Document> generate(
            final Path path,
            final MetadataSpecProperty metadataSpecProperty) {

        final var keys = new Document();
        keys.put("metadata." + join(".", path.getComponents()), 1);

        final var metadata = new MongoIndexMetadata();
        metadata.setKeys(keys);

        return metadata;

    }

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
