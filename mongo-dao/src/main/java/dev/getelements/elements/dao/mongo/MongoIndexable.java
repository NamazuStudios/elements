package dev.getelements.elements.dao.mongo;

import com.mongodb.client.MongoCollection;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexMetadata;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlan;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlanStep;
import dev.getelements.elements.dao.mongo.model.metadata.MongoMetadata;
import dev.getelements.elements.dao.mongo.model.schema.MongoMetadataSpec;
import dev.getelements.elements.rt.exception.InternalException;
import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.dao.Indexable;
import dev.getelements.elements.sdk.dao.index.IndexPlanner;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.index.IndexMetadata;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import jakarta.inject.Inject;
import org.bson.Document;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Function;

import static dev.getelements.elements.dao.mongo.MongoStandardProperties.*;
import static dev.getelements.elements.sdk.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.sdk.model.index.IndexPlanState.APPLIED;
import static dev.getelements.elements.sdk.model.index.IndexPlanState.READY;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public abstract class MongoIndexable<SpecT, IndexedT> implements Indexable {

    private static final Logger logger = LoggerFactory.getLogger(MongoDistinctInventoryItem.class);

    private MapperRegistry mapperRegistry;

    private Datastore datastore;

    protected final Class<SpecT> spec;

    protected final Class<IndexedT> indexed;

    protected final Function<SpecT, String> nameExtractor;

    protected final Function<SpecT, MongoMetadataSpec> metadataSpecExtractor;

    /**
     * Creates a new MongoIndexable instance with the default name and metadata spec extractors.
     *
     * @param spec the type that contains the specification and unique name
     * @param indexed the type that will be indexed based on the spec provided
     */
    public MongoIndexable(final Class<SpecT> spec, final Class<IndexedT> indexed) {
        this(spec, indexed, getNameExtractor(spec), getMetadataSpecExtractor(spec));
    }

    /**
     * Creates a new MongoIndexable instance with the default name and metadata spec extractors. This allows for
     * specification of custom name and metadata spec extractors in case the default bean properties ones are not
     * sufficient.
     *
     * @param spec the type that contains the specification and unique name
     * @param indexed the type that will be indexed based on the spec provided
     */
    public MongoIndexable(final Class<SpecT> spec,
                          final Class<IndexedT> indexed,
                          final Function<SpecT, String> nameExtractor,
                          final Function<SpecT, MongoMetadataSpec> metadataSpecExtractor) {
        this.spec = spec;
        this.indexed = indexed;
        this.nameExtractor = nameExtractor;
        this.metadataSpecExtractor = metadataSpecExtractor;
    }

    @Override
    public void plan() {

        final var collection = getCollectionName();

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

        updatePlanWithMetadataSpecs(plan);

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

    /**
     * Returns the collection which will be indexed.
     *
     * @return the collection
     */
    protected MongoCollection<IndexedT> getCollection() {
        return getDatastore().getCollection(indexed);
    }

    /**
     * Returns the name of the collection which will be indexed.
     *
     * @return the collection name
     */
    protected String getCollectionName() {
        return getDatastore()
                .getMapper()
                .getEntityModel(indexed)
                .getCollectionName();
    }

    /**
     * Generates the index metadata for the given path and metadata spec property.
     *
     * @param path                   the path to generate the index for
     * @param metadataSpecProperty   the metadata spec property to use
     * @return the generated index metadata
     */
    protected IndexMetadata<Document> generate(
            final Path path,
            final MetadataSpecProperty metadataSpecProperty) {

        final var keys = new Document();
        keys.put(METADATA_PROPERTY + "." + join(".", path.getComponents()), 1);

        final var metadata = new MongoIndexMetadata();
        metadata.setKeys(keys);

        return metadata;

    }

    /**
     * Updates the index plan with metadata specifications.
     *
     * @param planner the index planner to update
     */
    protected void updatePlanWithMetadataSpecs(final IndexPlanner<Document> planner) {
        getDatastore()
                .find(spec)
                .stream()
                .forEach(item -> {

                    final var name = nameExtractor.apply(item);
                    final var metadataSpec = metadataSpecExtractor.apply(item);

                    if (name != null && metadataSpec != null) {
                        final var spec = getMapper().map(metadataSpec, MetadataSpec.class);
                        planner.update(name, spec);
                    }

                });
    }

    @Override
    public void buildIndexes() {

        final var collection = getCollection();
        final var collectionName = getCollectionName();

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
            final MongoCollection<IndexedT> collection,
            final MongoIndexPlanStep step) {
        logger.info("Creating {}", step.getIndexMetadata().getIdentifier());
        collection.createIndex(step.getIndexMetadata().getKeys());
    }

    private void delete(
            final MongoCollection<IndexedT> collection,
            final MongoIndexPlanStep step) {
        logger.info("Deleting {}", step.getIndexMetadata().getIdentifier());
        collection.dropIndex(step.getIndexMetadata().getKeys());
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

    public static class Metadata extends MongoIndexable<MongoMetadata, MongoMetadata> {

        public Metadata() {
            super(MongoMetadata.class, MongoMetadata.class);
        }

    }

    public static class Item extends MongoIndexable<MongoItem, MongoItem> {
        public Item() {
            super(MongoItem.class, MongoItem.class);
        }
    }

    public static class DistinctInventoryItem extends MongoIndexable<MongoItem, MongoDistinctInventoryItem> {

        public DistinctInventoryItem() {
            super(MongoItem.class, MongoDistinctInventoryItem.class);
        }

        @Override
        protected void updatePlanWithMetadataSpecs(IndexPlanner<Document> planner) {
            getDatastore()
                    .find(spec)
                    .filter(eq("category", DISTINCT), exists(METADATA_SPEC_PROPERTY))
                    .stream()
                    .forEach(item -> {

                        final var name = item.getName();
                        final var metadataSpec = item.getMetadataSpec();

                        if (name != null && metadataSpec != null) {
                            final var spec = getMapper().map(metadataSpec, MetadataSpec.class);
                            planner.update(name, spec);
                        }

                    });
        }

    }

}
