package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.schema.template.MetadataSpec;
import dev.getelements.elements.model.schema.template.TemplateFieldType;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.template.TemplateTabField;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.model.schema.template.TemplateFieldType.ARRAY;
import static dev.getelements.elements.model.schema.template.TemplateFieldType.OBJECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Guice(modules = IntegrationTestModule.class)
public class MongoIndexDaoTest {

    private IndexDao indexDao;

    private ItemTestFactory itemTestFactory;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    private Item testItem;

    private MetadataSpec testMetadataSpec;

    @BeforeClass
    public void setupMetadataSpec() {
        final var generator = new MockSpecGenerator();
        testMetadataSpec = getMetadataSpecTestFactory().createTestSpec("test_index", generator::generate);
    }

    @BeforeClass(dependsOnMethods = "setupMetadataSpec")
    public void setupDistinctInventoryItem() {
        testItem = getItemTestFactory().createTestItem("index_test", item -> {
            item.setDescription("Indexable Item Test.");
            item.setTags(new ArrayList<>());
            item.setCategory(DISTINCT);
            item.setMetadataSpec(testMetadataSpec);
            return item;
        });
    }

    @Test
    public void testPlan() {
        getIndexDao().plan();
    }

    @Test(dependsOnMethods = "testPlan")
    public void testBuildAllCustom() {
        try (var indexer = getIndexDao().beginIndexing()) {
            indexer.buildAllCustom();
        }
    }

    public IndexDao getIndexDao() {
        return indexDao;
    }

    @Inject
    public void setIndexDao(IndexDao indexDao) {
        this.indexDao = indexDao;
    }

    public ItemTestFactory getItemTestFactory() {
        return itemTestFactory;
    }

    @Inject
    public void setItemTestFactory(ItemTestFactory itemTestFactory) {
        this.itemTestFactory = itemTestFactory;
    }

    public MetadataSpecTestFactory getMetadataSpecTestFactory() {
        return metadataSpecTestFactory;
    }

    @Inject
    public void setMetadataSpecTestFactory(MetadataSpecTestFactory metadataSpecTestFactory) {
        this.metadataSpecTestFactory = metadataSpecTestFactory;
    }

    private class MockSpecGenerator {

        final Deque<TemplateTab> depth = new LinkedList<>();

        private TemplateTabField fieldsSupplier(final TemplateFieldType type) {

            final var field = new TemplateTabField();
            field.setFieldType(type);
            field.setRequired(true);
            field.setName(format("f%s", type));
            field.setDisplayName(format("Test Field for %s", type));

            if (OBJECT.equals(type)) {

                final List<TemplateTab> tabs = depth.size() < 2
                    ? IntStream.range(0, 5).mapToObj(this::tabSupplier).collect(toList())
                    : List.of();

                field.setTabs(tabs);

            }

            if (ARRAY.equals(type)) {
                final var tabs = List.of(tabSupplier(0));
                field.setTabs(tabs);
            }

            return field;

        };

        private TemplateTab tabSupplier(final int index) {

            final var tab = new TemplateTab();
            depth.push(tab);
            tab.setTabOrder(index);
            tab.setName(format("f%s", index));

            final var fields = Stream.of(TemplateFieldType.values())
                    .filter(t -> !ARRAY.equals(t) || depth.size() < 2)
                    .map(this::fieldsSupplier)
                    .collect(toMap(TemplateTabField::getName, f -> f));

            tab.setFields(fields);
            return depth.pop();

        }

        public MetadataSpec generate(final MetadataSpec spec) {

            final var tabs = IntStream.range(0, 5)
                    .mapToObj(this::tabSupplier)
                    .collect(toList());

            spec.setTabs(tabs);
            return spec;

        }

    }
}
