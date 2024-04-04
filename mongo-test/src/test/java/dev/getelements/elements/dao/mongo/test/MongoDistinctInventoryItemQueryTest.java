package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.dao.mongo.model.goods.MongoDistinctInventoryItem;
import dev.getelements.elements.dao.mongo.test.ItemTestFactory;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.MetadataSpecBuilder;
import dev.morphia.Datastore;
import dev.morphia.UpdateOptions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.*;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static dev.getelements.elements.model.index.IndexableType.DISTINCT_INVENTORY_ITEM;
import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static dev.morphia.query.updates.UpdateOperators.set;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoDistinctInventoryItemQueryTest {

    private static final Logger logger = LoggerFactory.getLogger(MongoDistinctInventoryItemQueryTest.class);

    private IndexDao indexDao;

    private ItemTestFactory itemTestFactory;

    private UserTestFactory userTestFactory;

    private ProfileTestFactory profileTestFactory;

    private ApplicationTestFactory applicationTestFactory;

    private DistinctInventoryItemDao distinctInventoryItemDao;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    private Item testItem;

    private List<User> users;

    private List<Profile> profiles;

    private Application application;

    private MetadataSpec testMetadataSpec;

    private List<DistinctInventoryItem> distinctInventoryItems;

    private Datastore datastore;

    @BeforeClass
    public void setupTestUsers() {
        users = range(0, 5)
                .mapToObj(i -> getUserTestFactory().createTestUser())
                .collect(toList());
    }

    @BeforeClass
    public void setupTestApplication() {
        application = getApplicationTestFactory().createMockApplication("Test for Queries.");
    }

    @BeforeClass(dependsOnMethods = {"setupTestUsers", "setupTestApplication"})
    public void setupTestProfiles() {
        profiles = users.stream()
                .map(user -> getProfileTestFactory().makeMockProfile(user, application))
                .collect(toList());
    }

    @BeforeClass
    public void setupMetadataSpec() {
        testMetadataSpec = getMetadataSpecTestFactory().createTestSpec("test_spec", spec ->
            MetadataSpecBuilder.with(spec)
                .type(OBJECT)
                .properties()
                    .property()
                        .name("test_string").type(STRING).displayName("String Field.").required(true)
                    .endProperty()
                    .property()
                        .name("test_number").type(NUMBER).displayName("Numeric Field.").required(true)
                    .endProperty()
                    .property()
                        .name("test_object").type(OBJECT).displayName("Nested Object Field.").required(true)
                        .properties()
                            .property()
                                .name("test_string").type(STRING).displayName("String Field.").required(true)
                            .endProperty()
                            .property()
                                .name("test_number").type(NUMBER).displayName("Numeric Field.").required(true)
                            .endProperty()
                        .endProperties()
                    .endProperty()
                    .property()
                        .name("test_array").type(ARRAY).displayName("Nested Object Field.").required(true)
                            .properties()
                                .property()
                                    .name("_arr").type(STRING).displayName("String Field.").required(true)
                                .endProperty()
                            .endProperties()
                        .endProperty()
                    .endProperties()
            .endMetadataSpec()
        );
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

    @BeforeClass(dependsOnMethods = {"setupDistinctItem", "setupTestProfiles"})
    public void setupDistinctInventoryItems() {
        distinctInventoryItems = profiles.stream().flatMap(
                profile -> range(0, 100).mapToObj(index -> {

                    final var item = new DistinctInventoryItem();
                    item.setItem(testItem);
                    item.setUser(profile.getUser());
                    item.setProfile(profile);

                    final var nested = new LinkedHashMap<String, Object>();
                    nested.put("test_number", index);
                    nested.put("test_string", format("%s", index));

                    final var metadata = new LinkedHashMap<String, Object>();
                    metadata.put("test_object", nested);
                    metadata.put("test_number", index);
                    metadata.put("test_string", format("%s", index));

                    item.setMetadata(metadata);
                    return getDistinctInventoryItemDao().createDistinctInventoryItem(item);

                }
        )).collect(toList());
    }

    @DataProvider
    public Object[][] getUsersAndProfiles() {
        return profiles.stream()
                .map(p -> new Object[]{p.getUser(), p})
                .toArray(Object[][]::new);
    }

    @AfterMethod
    public void testBogusIndex() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null, false,
                "metadata.test_bogus:0"
        );

        assertEquals(result.getTotal(), 0);
        assertEquals(result.getObjects().size(), 0);

    }

    @Test(groups = "noIndexes")
    public void testSearchUnindexedReturnsNoResults() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null, false,
                "metadata.test_string:0"
        );

        assertEquals(result.getTotal(), 0);
        assertEquals(result.getObjects().size(), 0);

    }

    @Test(groups = "firstIndex", dependsOnGroups = "noIndexes")
    public void firstIndex() {

        indexDao.planType(DISTINCT_INVENTORY_ITEM);

        try (var indexer = indexDao.beginIndexing()) {
            indexer.buildCustomIndexesFor(DISTINCT_INVENTORY_ITEM);
        }

    }

    @Test(groups = "firstIndexQuery", dependsOnGroups = "firstIndex")
    public void testSearchIndexedReturnsResults() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null, false,
                "metadata.test_string:0"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

    }

    @Test(groups = "firstIndexQuery", dependsOnGroups = "firstIndex")
    public void testSearchIndexedReturnsResultsNested() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_object.test_string:0"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

    }

    @Test(groups = "firstIndexQuery", dependsOnGroups = "firstIndex")
    public void testNumericQuery() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_object.test_number = 0"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

    }

    @Test(groups = "firstIndexQuery", dependsOnGroups = "firstIndex")
    public void testComplexQuery() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_number > 1 AND metadata.test_number < 15"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

        result.getObjects().forEach(i -> {
            final var number = (Integer) i.getMetadata().get("test_number");
            assertTrue(number > 1);
            assertTrue(number < 15);
        });

    }

    @Test(groups = "updateMetadataSpec", dependsOnGroups = "firstIndexQuery")
    public void updateMetadataSpec() {

        final var property = new MetadataSpecProperty();
        property.setType(STRING);
        property.setName("test_new_string_property");
        property.setDisplayName("Test New Property");

        final var properties = new ArrayList<>(testMetadataSpec.getProperties());
        properties.add(property);
        properties.removeIf(p -> ARRAY.equals(p.getType()));
        properties.removeIf(p -> OBJECT.equals(p.getType()));

        testMetadataSpec.setProperties(properties);

        testMetadataSpec = getMetadataSpecTestFactory()
                .getMetadataSpecDao()
                .updateActiveMetadataSpec(testMetadataSpec);

    }

    @Test(groups = "secondIndex", dependsOnGroups = "updateMetadataSpec")
    public void secondIndex() {

        indexDao.planType(DISTINCT_INVENTORY_ITEM);

        try (var indexer = indexDao.beginIndexing()) {
            indexer.buildCustomIndexesFor(DISTINCT_INVENTORY_ITEM);
        }

    }

    @Test(groups = "secondIndexQuery", dependsOnGroups = "secondIndex")
    public void testOldNestedQueryFails() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_object.test_string:0"
        );

        assertEquals(result.getTotal(), 0);
        assertTrue(result.getObjects().isEmpty());

    }

    @Test(groups = "secondIndexQuery", dependsOnGroups = "secondIndex")
    public void testSecondNumericQuery() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_number = 0"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

    }

    @Test(groups = "secondIndexQuery", dependsOnGroups = "secondIndex")
    public void testUpdateMetadata() {

        final var options = new UpdateOptions()
                .upsert(false);

        getDatastore()
                .find(MongoDistinctInventoryItem.class)
                .update(options, set("metadata.test_new_string_property", "Hello"));

    }

    @Test(groups = "secondIndexQuery", dependsOnGroups = "secondIndex", dependsOnMethods = "testUpdateMetadata")
    public void testNewPropertyIndexesProperly() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,false,
                "metadata.test_new_string_property:Hello"
        );

        assertTrue(result.getTotal() > 0);
        assertFalse(result.getObjects().isEmpty());

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

    public Item getTestItem() {
        return testItem;
    }

    @Inject
    public void setTestItem(Item testItem) {
        this.testItem = testItem;
    }

    public MetadataSpec getTestMetadataSpec() {
        return testMetadataSpec;
    }

    @Inject
    public void setTestMetadataSpec(MetadataSpec testMetadataSpec) {
        this.testMetadataSpec = testMetadataSpec;
    }

    public DistinctInventoryItemDao getDistinctInventoryItemDao() {
        return distinctInventoryItemDao;
    }

    @Inject
    public void setDistinctInventoryItemDao(DistinctInventoryItemDao distinctInventoryItemDao) {
        this.distinctInventoryItemDao = distinctInventoryItemDao;
    }

    public ProfileTestFactory getProfileTestFactory() {
        return profileTestFactory;
    }

    @Inject
    public void setProfileTestFactory(ProfileTestFactory profileTestFactory) {
        this.profileTestFactory = profileTestFactory;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }

    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }

    public ApplicationTestFactory getApplicationTestFactory() {
        return applicationTestFactory;
    }

    @Inject
    public void setApplicationTestFactory(ApplicationTestFactory applicationTestFactory) {
        this.applicationTestFactory = applicationTestFactory;
    }

    public Datastore getDatastore() {
        return datastore;
    }

    @Inject
    public void setDatastore(Datastore datastore) {
        this.datastore = datastore;
    }

}
