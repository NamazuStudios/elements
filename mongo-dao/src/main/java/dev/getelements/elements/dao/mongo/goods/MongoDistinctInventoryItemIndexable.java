package dev.getelements.elements.dao.mongo.goods;

import com.mongodb.client.MongoCollection;
import dev.getelements.elements.dao.Indexable;
import dev.getelements.elements.model.index.IndexMetadata;
import dev.getelements.elements.dao.index.IndexPlanner;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.model.goods.MongoItem;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexMetadata;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlan;
import dev.getelements.elements.dao.mongo.model.index.MongoIndexPlanStep;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;
import dev.getelements.elements.rt.Path;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import org.bson.Document;
import org.dozer.Mapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Inject;

import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.morphia.query.filters.Filters.eq;
import static dev.morphia.query.filters.Filters.exists;
import static dev.morphia.query.updates.UpdateOperators.set;
import static dev.morphia.query.updates.UpdateOperators.unset;
import static java.lang.String.join;
import static java.util.stream.Collectors.toList;

public class MongoDistinctInventoryItemIndexable implements Indexable {

    private static final Logger logger = LoggerFactory.getLogger(MongoDistinctInventoryItem.class);

    private Mapper mapper;

    private Datastore datastore;

    @Override
    public void plan() {
        try (var session = getDatastore().startSession()) {

            final var collection = session
                    .getMapper()
                    .getEntityModel(MongoDistinctInventoryItem.class)
                    .getCollectionName();

            final var existing = session.find(MongoIndexPlan.class)
                    .filter(eq("_id", collection))
                    .stream()
                    .findFirst();

            final var plan = existing
                    .map(p -> new IndexPlanner.Builder<Document>()
                            .withExisting(p.getExisting())
                            .build(this::generate)
                    )
                    .orElseGet(() -> new IndexPlanner.Builder<Document>()
                            .build(this::generate)
                    );

            session.find(MongoItem.class)
                    .filter(eq("category", DISTINCT), exists("metadataSpec"))
                    .stream()
                    .forEach(item -> {
                        final var spec = getMapper().map(item.getMetadataSpec(), MetadataSpec.class);
                        plan.update(item.getName(), spec);
                    });

            final var steps = plan.getFinalPlan()
                            .stream()
                            .map(step -> getMapper().map(step, MongoIndexPlanStep.class))
                            .collect(toList());

            final var options = new UpdateOptions();

            session.find(MongoIndexPlan.class)
                    .filter(eq("_id", collection))
                    .update(options, set("steps", steps));

        }
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
                .filter(eq("_id", collection))
                .update(options, unset("steps"), set("existing", existing));

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
            final TemplateTab templateTab,
            final TemplateTabField templateTabField) {

        final var keys = new Document();
        keys.put("metadata." + join(".", path.getComponents()), 1);

        final var metadata = new MongoIndexMetadata();
        metadata.setKeys(keys);

        return metadata;

    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
