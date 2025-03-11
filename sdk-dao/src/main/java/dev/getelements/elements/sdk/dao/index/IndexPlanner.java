package dev.getelements.elements.sdk.dao.index;

import dev.getelements.elements.sdk.model.index.IndexMetadata;
import dev.getelements.elements.sdk.model.index.IndexPlanStep;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.MetadataSpecProperty;
import dev.getelements.elements.sdk.cluster.path.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

import static dev.getelements.elements.sdk.model.index.IndexOperation.*;
import static java.lang.String.format;

/**
 * Plans index updates in a database-agnostic way.
 */
public class IndexPlanner<IdentifierT> {

    private Logger logger = LoggerFactory.getLogger(IndexPlanner.class);

    private final IndexMetadataGenerator<IdentifierT> generator;

    private final Map<IdentifierT, IndexPlanStep<IdentifierT>> plan = new LinkedHashMap<>();

    private final Map<IdentifierT, IndexMetadata<IdentifierT>> existing = new LinkedHashMap<>();

    private IndexPlanner(
            final List<IndexMetadata<IdentifierT>> existing,
            final IndexMetadataGenerator<IdentifierT> generator) {

        this.generator = generator;

        for (var metadata: existing) {
            this.existing.put(metadata.getIdentifier(), metadata);
        }

    }

    /**
     * Updates the indexing plan with the given metadata specification.
     *
     * @param context
     * @param spec
     * @return
     */
    public IndexPlanner<IdentifierT> update(final String context, final MetadataSpec spec) {

        logger.info("Updating plan with spec: {}", spec);

        final var updates = new MetadataSpecPlanner(context, spec).build();

        for (var entry : updates.entrySet()) {

            final var update = entry.getValue();
            final var existing = this.existing.get(entry.getKey());

            if (existing == null) {
                logger.info("Adding New Index: {}", update);
                update.setOperation(CREATE);
            } else if (Objects.equals(existing, update.getIndexMetadata())){
                logger.info("Leaving Index As-Is: {}", update);
                update.setOperation(LEAVE_AS_IS);
            } else {
                logger.info("Replacing Index: {}", update);
                update.setOperation(REPLACE);
            }

            final var evicted = plan.put(update.getIndexMetadata().getIdentifier(), update);

            if (evicted != null) {
                logger.info("Evicted previous plan step {} (duplicate index).", evicted);
            }

        }

        return this;

    }

    /**
     * Calculates the final plan based on the updates applied to the plan so far.
     *
     * @return the final plan
     */
    public List<IndexPlanStep<IdentifierT>> getFinalExecutionSteps() {

        final var finalPlan = new LinkedHashMap<>(plan);

        final var toRemove = new LinkedHashMap<>(existing);

        toRemove.keySet().removeAll(plan.keySet());
        toRemove.forEach((name, metadata) -> {
            final var step = new IndexPlanStep<IdentifierT>();
            step.setOperation(DELETE);
            step.setIndexMetadata(metadata);
            step.setDescription(format("Unused Index %s", metadata.getIdentifier()));
            finalPlan.put(name, step);
        });

        return new ArrayList<>(finalPlan.values());

    }

    private class MetadataSpecPlanner {

        private final String context;

        private final MetadataSpec spec;

        private final Deque<MetadataSpecProperty> properties = new LinkedList<>();

        private final Map<IdentifierT, IndexPlanStep<IdentifierT>> steps = new LinkedHashMap<>();

        public MetadataSpecPlanner(final String context, final MetadataSpec spec) {
            this.spec = spec;
            this.context = context;
        }


        public Map<IdentifierT, IndexPlanStep<IdentifierT>> build() {
            final var properties = spec.getProperties();
            if (properties != null) properties.forEach(this::build);
            return steps;
        }

        private void build(final MetadataSpecProperty property) {

            properties.addLast(property);

            final var components = new ArrayList<String>();
            properties.forEach(p -> components.add(p.getName()));

            final var path = new Path(context, components);
            final var description = format("Index for path %s", path);
            final var metadata = generator.generate(path, property);

            final var step = new IndexPlanStep<IdentifierT>();
            step.setIndexMetadata(metadata);
            step.setDescription(description);

            steps.put(metadata.getIdentifier(), step);
            logger.info("Index from Metadata Field {} : {}", property, step);

            final var subProperties = property.getProperties();
            if (subProperties != null) subProperties.forEach(this::build);

            properties.removeLast();

        }

    }

    public static class Builder<IdentifierT> {

        private List<IndexMetadata<IdentifierT>> existing = new ArrayList<>();

        public Builder<IdentifierT> withExisting(final List<? extends IndexMetadata<IdentifierT>> existing) {
            this.existing.addAll(existing);
            return this;
        }

        public IndexPlanner<IdentifierT> build(final IndexMetadataGenerator<IdentifierT> generator) {
            return new IndexPlanner<>(existing, generator);
        }

    }

    @FunctionalInterface
    public interface IndexMetadataGenerator<IdentifierT> {

        IndexMetadata<IdentifierT> generate(Path path, MetadataSpecProperty field);

    }

}
