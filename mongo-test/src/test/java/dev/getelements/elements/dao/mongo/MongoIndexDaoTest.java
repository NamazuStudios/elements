package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;

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

        private int depth = 0;

        private MetadataSpecProperty property(final MetadataSpecPropertyType type) {

            depth ++;

            final var property = new MetadataSpecProperty();
            property.setType(type);
            property.setRequired(true);
            property.setName(format("f_%s", type).toLowerCase());
            property.setDisplayName(format("Test Field for %s", type));

            if (OBJECT.equals(type)) {

                final List<MetadataSpecProperty> properties = depth < 2
                    ? Stream.of(MetadataSpecPropertyType.values()).map(this::property).collect(toList())
                    : List.of();

                property.setProperties(properties);

            }

            if (ARRAY.equals(type)) {
                final var properties = List.of(property(STRING));
                property.setProperties(properties);
            }

            depth--;

            return property;

        };

        public MetadataSpec generate(final MetadataSpec spec) {

            final List<MetadataSpecProperty> properties = Stream
                    .of(MetadataSpecPropertyType.values())
                    .map(this::property)
                    .collect(toList());

            spec.setType(OBJECT);
            spec.setProperties(properties);
            return spec;

        }

    }
}
