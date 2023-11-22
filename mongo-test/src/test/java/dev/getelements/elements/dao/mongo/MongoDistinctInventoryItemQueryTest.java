package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.DistinctInventoryItemDao;
import dev.getelements.elements.dao.IndexDao;
import dev.getelements.elements.model.application.Application;
import dev.getelements.elements.model.goods.Item;
import dev.getelements.elements.model.inventory.DistinctInventoryItem;
import dev.getelements.elements.model.profile.Profile;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.user.User;
import dev.getelements.elements.util.MetadataSpecBuilder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static dev.getelements.elements.dao.IndexDao.IndexableType.DISTINCT_INVENTORY_ITEM;
import static dev.getelements.elements.model.goods.ItemCategory.DISTINCT;
import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static java.lang.String.format;
import static java.util.stream.Collectors.toList;
import static java.util.stream.IntStream.range;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

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
                                    .name("test_string").type(STRING).displayName("String Field.").required(true)
                                .endProperty()
                                .property()
                                    .name("test_number").type(NUMBER).displayName("Numeric Field.").required(true)
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

//    @Test
//    public void testSearchUnindexedReturnsNoResults() {
//
//        logger.info("Searching items.");
//
//        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
//                0, 100,
//                null, null,
//                "metadata.test_string:0"
//        );
//
//        assertEquals(result.getTotal(), 0);
//        assertEquals(result.getObjects().size(), 0);
//
//    }

    @Test// (dependsOnMethods = "testSearchUnindexedReturnsNoResults")
    public void generateIndexes() {

        indexDao.planType(DISTINCT_INVENTORY_ITEM);

        try (var indexer = indexDao.beginIndexing()) {
            indexer.buildCustomIndexesFor(DISTINCT_INVENTORY_ITEM);
        }

    }

//    @Test(dependsOnMethods = "generateIndexes")
//    public void testSearchIndexedReturnsResults() {
//
//        logger.info("Searching items.");
//
//        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
//                0, 100,
//                null, null,
//                "metadata.test_string:0"
//        );
//
//        assertTrue(result.getTotal() > 0);
//        assertTrue(!result.getObjects().isEmpty());
//
//    }

    @Test(dependsOnMethods = "generateIndexes")
    public void testSearchIndexedReturnsResultsNested() {

        logger.info("Searching items.");

        final var result = getDistinctInventoryItemDao().getDistinctInventoryItems(
                0, 100,
                null, null,
                "metadata.test_object.test_string:0"
        );

        assertTrue(result.getTotal() > 0);
        assertTrue(!result.getObjects().isEmpty());

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

}
