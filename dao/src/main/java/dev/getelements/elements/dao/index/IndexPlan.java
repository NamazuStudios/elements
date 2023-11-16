package dev.getelements.elements.dao.index;

import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;
import dev.getelements.elements.rt.exception.InternalException;
import org.bouncycastle.util.encoders.Hex;

import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.*;
import java.util.function.Function;
import java.util.function.Supplier;

import static dev.getelements.elements.dao.index.IndexOperation.CREATE;
import static java.lang.String.format;
import static java.lang.String.join;

public class IndexPlan {

    private final int nameLimit;

    private final String separator;

    private final Supplier<MessageDigest> messageDigestSupplier;

    private final Map<String, IndexStep> plan = new LinkedHashMap<>();

    private final Charset charset;

    private Function<byte[], String> encoder;

    private IndexPlan(final int nameLimit,
                      final String separator,
                      final Supplier<MessageDigest> messageDigestSupplier,
                      final Charset charset,
                      final Function<byte[], String> encoder) {
        this.charset = charset;
        this.nameLimit = nameLimit;
        this.separator = separator;
        this.messageDigestSupplier = messageDigestSupplier;
        this.encoder = encoder;
    }

    public IndexPlan add(final String context, final MetadataSpec spec) {

        final var names = new TreeSet<>(plan.keySet());
        final var steps = new MetadataSpecPlanner(context, spec).build();

        names.retainAll(steps.keySet());

        if (!names.isEmpty()) {
            final var joined = join(",", names);
            throw new InternalException("Collision on index names [" + joined + "]");
        }

        plan.putAll(steps);
        return this;

    }

    public Map<String, IndexStep> getPlan() {
        return Collections.unmodifiableMap(plan);
    }

    private class MetadataSpecPlanner {

        private final String context;

        private final MetadataSpec spec;

        private final Deque<TemplateTab> tabs = new LinkedList<>();

        private final SortedMap<String, IndexStep> steps = new TreeMap<>();

        public MetadataSpecPlanner(final String context, final MetadataSpec spec) {
            this.spec = spec;
            this.context = context;
        }

        public SortedMap<String, IndexStep> build() {

            for (var tab : spec.getTabs()) {
                tab.getFields().values().forEach(field -> build(tab, field));
            }

            return steps;

        }

        private void build(final TemplateTab tab, final TemplateTabField field) {

            tabs.push(tab);

            switch (field.getFieldType()) {
                case OBJECT:
                    buildObjectIndex(tab, field);
                    break;
                default:
                    buildTabIndex(tab, field);
                    break;
            }

        }

        private void buildTabIndex(final TemplateTab tab, final TemplateTabField field) {

            final var digest = messageDigestSupplier.get();
            final var components = new ArrayList<String>();

            // Creates the full path
            components.add(context);
            tabs.forEach(t -> components.add(t.getName()));
            components.add(field.getName());

            // Creates the digest of the full path
            components.forEach(c -> digest.update(c.getBytes(charset)));

            final var suffix = join(separator, components);
            final var prefix = encoder.apply(digest.digest());
            final var path = format("%s%s%s", prefix, separator, suffix);
            final var truncated = path.length() <= nameLimit ? path : path.substring(0, nameLimit);
            final var description = format("%s %s", CREATE, join( ".", components));

            final var step = new SimpleIndexStep();
            step.setIndexName(truncated);
            step.setDescription(description);
            step.setTemplateTabField(field);
            steps.put(truncated, step);

        }

        private void buildObjectIndex(final TemplateTab tab, final TemplateTabField field) {
            for (var child : field.getTabs()) {
                tab.getFields().values().forEach(f -> build(child, f));
            }
        }

    }

    public static class Builder {

        private int nameLimit = 127;

        private String separator = "_";

        private Charset charset = StandardCharsets.UTF_8;

        private Function<byte[], String> encoder = b -> Hex.toHexString(b);

        private Supplier<MessageDigest> messageDigestSupplier = () -> {
            try {
                return MessageDigest.getInstance("SHA_1");
            } catch (NoSuchAlgorithmException e) {
                throw new UnsupportedOperationException(e);
            }
        };

        public IndexPlan build() {
            return new IndexPlan(
                    nameLimit,
                    separator,
                    messageDigestSupplier,
                    charset,
                    encoder
            );
        }

    }

}
