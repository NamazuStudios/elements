package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import dev.getelements.elements.model.schema.template.TemplateTab;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
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
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ARRAY;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.OBJECT;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toMap;

@Guice(modules = IntegrationTestModule.class)
public class MongoIndexDaoTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoIndexDaoTest.class);

    private IndexDao indexDao;

    private ItemTestFactory itemTestFactory;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    private Item testItem;

    private MetadataSpec testMetadataSpec;

    @BeforeClass
    public void setupMetadataSpec() {
        final var generator = new MockMetadataSpecGenerator();
        testMetadataSpec = getMetadataSpecTestFactory().createTestSpec("test_index", generator::generate);
    }

    @BeforeClass(dependsOnMethods = "setupMetadataSpec")
    public void setupDistinctItem() {
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
        getIndexDao().planAll();
    }

    @Test(dependsOnMethods = "testPlan")
    public void testBuildAllCustom() {
        try (var indexer = getIndexDao().beginIndexing()) {
            indexer.buildAllCustom();
        }
    }

    @AfterClass
    public void logResult() {
        logger.info("Done indexing!");
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

    private static class MockMetadataSpecGenerator {

        final Deque<TemplateTab> depth = new LinkedList<>();

        private MetadataSpecProperty fieldsSupplier(final MetadataSpecPropertyType type) {

            final var field = new MetadataSpecProperty();
            field.setType(type);
            field.setRequired(true);
            field.setName(format("f_%s", type).toLowerCase());
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

            final var fields = Stream.of(MetadataSpecPropertyType.values())
                    .filter(t -> !ARRAY.equals(t) || depth.size() < 2)
                    .map(this::fieldsSupplier)
                    .collect(toMap(MetadataSpecProperty::getName, f -> f));

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
