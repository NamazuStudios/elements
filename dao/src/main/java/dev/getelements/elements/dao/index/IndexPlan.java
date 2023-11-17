package dev.getelements.elements.dao.index;

import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.TemplateFieldType;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;
import dev.getelements.elements.rt.Path;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.Identity;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.getelements.elements.dao.index.IndexOperation.*;
import static java.lang.String.format;

/**
 * Plans index updates in a database-agnostic way.
 */
public class IndexPlan<IdentifierT> {

    private Logger logger = LoggerFactory.getLogger(IndexPlan.class);

    private final IndexMetadataGenerator<IdentifierT> generator;

    private final Map<IdentifierT, IndexPlanStep<IdentifierT>> plan = new LinkedHashMap<>();

    private final Map<IdentifierT, IndexMetadata<IdentifierT>> existing = new LinkedHashMap<>();

    private Function<byte[], String> encoder;

    private IndexPlan(
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
    public IndexPlan update(final String context, final MetadataSpec spec) {

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
                logger.info("Evicted previous plan step {}", evicted);
            }

        }

        return this;

    }

    /**
     * Calculates the final plan based on the updates applied to the plan so far.
     *
     * @return the final plan
     */
    public List<IndexPlanStep<IdentifierT>> getFinalPlan() {

        final var finalPlan = new LinkedHashMap<>(plan);

        final var toRemove = new LinkedHashMap<>(existing);

        toRemove.keySet().removeAll(plan.keySet());
        toRemove.forEach((name, metadata) -> {
            final var step = new IndexPlanStep();
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

        private final Deque<TemplateTab> tabs = new LinkedList<>();

        private final Map<IdentifierT, IndexPlanStep<IdentifierT>> steps = new LinkedHashMap<>();

        public MetadataSpecPlanner(final String context, final MetadataSpec spec) {
            this.spec = spec;
            this.context = context;
        }


        public Map<IdentifierT, IndexPlanStep<IdentifierT>> build() {

            for (var tab : spec.getTabs()) {
                tab.getFields().values().forEach(field -> build(tab, field));
            }

            return steps;

        }

        private void build(final TemplateTab tab, final TemplateTabField field) {

            tabs.push(tab);

            if (Objects.requireNonNull(field.getFieldType()) == TemplateFieldType.OBJECT) {
                buildObjectIndex(tab, field);
            } else {
                buildTabIndex(tab, field);
            }

        }

        private void buildTabIndex(final TemplateTab tab, final TemplateTabField field) {

            final var components = new ArrayList<String>();

            // Creates the full path
            tabs.forEach(t -> components.add(t.getName()));
            components.add(field.getName());

            final var path = new Path(context, components);
            final var description = format("Index for path %s", path);
            final var metadata = generator.generate(path, tab, field);

            final var step = new IndexPlanStep<IdentifierT>();
            step.setIndexMetadata(metadata);
            step.setDescription(description);
            step.setTemplateTabField(field);

            steps.put(metadata.getIdentifier(), step);
            logger.info("Index from Metadata Field {} : {}", field, step);

        }

        private void buildObjectIndex(final TemplateTab tab, final TemplateTabField field) {
            for (var child : field.getTabs()) {
                tab.getFields().values().forEach(f -> build(child, f));
            }
        }

    }

    public static class Builder<IdentifierT> {

        private List<IndexMetadata<IdentifierT>> existing = new ArrayList<>();

        public Builder<IdentifierT> withExisting(final List<? extends IndexMetadata<IdentifierT>> existing) {
            this.existing.addAll(existing);
            return this;
        }

        private Supplier<MessageDigest> messageDigestSupplier = () -> {
            try {
                return MessageDigest.getInstance("SHA_1");
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedOperationException(e);
            }
        };

        public IndexPlan<IdentifierT> build(final IndexMetadataGenerator<IdentifierT> generator) {
            return new IndexPlan<>(existing, generator);
        }

    }

    @FunctionalInterface
    public interface IndexMetadataGenerator<IdentifierT> {

        IndexMetadata<IdentifierT> generate(Path path, TemplateTab tab, TemplateTabField field);

    }

}
