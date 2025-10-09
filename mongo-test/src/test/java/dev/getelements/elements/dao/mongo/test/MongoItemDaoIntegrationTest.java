package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.ItemDao;
import dev.getelements.elements.sdk.model.exception.DuplicateException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.morphia.Datastore;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.testng.annotations.*;
import org.testng.collections.Lists;

import jakarta.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static dev.getelements.elements.sdk.model.goods.ItemCategory.FUNGIBLE;
import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoItemDaoIntegrationTest {

    private ItemDao itemDao;

    private Datastore Datastore;

    private MatchingMockObjects matchingMockObjects;

    @Test
    public void testCreateAndRead() {

        final var item = createMockItem("create_and_read");
        final var createdItem = itemDao.createItem(item);
        assertNotNull(createdItem.getId());

        final var byName = itemDao.getItemByIdOrName(createdItem.getName());
        assertEquals(byName, createdItem);

        final var byId = itemDao.getItemByIdOrName(createdItem.getId());
        assertEquals(byId, createdItem);

    }

    @Test
    public void testCreateAndNonStringMetadataValue() {
        final var item = createMockItem("create_and_read_non_string_metadata");
        final var createdItem = itemDao.createItem(item);
        assertEquals(createdItem.getMetadata().get("hamCount"), 2000000);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetItemByIdNotFound() {
        itemDao.getItemByIdOrName(new ObjectId().toHexString());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetItemByNameNotFound() {
        itemDao.getItemByIdOrName("non_existent_name");
    }

    @Test(expectedExceptions = DuplicateException.class)
    public void testCreateNameAlreadyExists() {
        final var item = createMockItem("create_name_already_exists");
        final var created = itemDao.createItem(item);
        assertNotNull(created);
        itemDao.createItem(item);
    }

    @Test
    public void testModify() {

        final var item = createMockItem("modify");
        final var createdItem = itemDao.createItem(item);
        createdItem.setDisplayName("Different Display Name");

        final var fromUpdate = itemDao.updateItem(createdItem);
        assertEquals(fromUpdate.getDisplayName(), "Different Display Name");

        final var lookedUpItem = itemDao.getItemByIdOrName(createdItem.getId());
        assertEquals(fromUpdate, lookedUpItem);

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testModifyNotFound() {
        final var item = createMockItem("modify_not_found");
        item.setId(new ObjectId().toHexString());
        itemDao.updateItem(item);
    }

    @Test
    public void testGetItemsByTags() {
        int numberOfItems = 50;
        List<String> tagsToFilterBy = Lists.newArrayList("laser", "tag");

        //Add Items to collection, and track the objectIds that contain the tags we care about.
        Set<ObjectId> expectedObjectIds = new HashSet<>();
        for (int i = 0; i < numberOfItems; i++) {
            Item item = createMockItem(format("items_by_tags_%d", i));
            List<String> tagsForItem = new ArrayList<>();
            item.setName(RandomStringUtils.randomAlphabetic(30));
            if (i % 2 == 0) {
                tagsForItem.add("laser");
                tagsForItem.add("tag");
            }
            if (i % 3 == 0) {
                //Throw in a dummy tag
                tagsForItem.add("arena");
            }
            item.setTags(tagsForItem);
            Item createdItem = itemDao.createItem(item);
            if (i % 2 == 0) {
                expectedObjectIds.add(new ObjectId(createdItem.getId()));
            }
        }

        //Call Pagination and verify that all items are returned
        Set<ObjectId> seen = new HashSet<>();
        Pagination<Item> itemPagination = itemDao.getItems(0, Integer.MAX_VALUE, tagsToFilterBy, null, null);
        while (itemPagination.getOffset() < itemPagination.getTotal()) {
            int nextOffset = itemPagination.getObjects().size() + itemPagination.getOffset();
            seen.addAll(itemPagination.getObjects().stream()
                .map(item -> new ObjectId(item.getId())).collect(Collectors.toList()));
            itemPagination = itemDao.getItems(nextOffset, Integer.MAX_VALUE, tagsToFilterBy, null, null);
        }

        assertEquals(seen, expectedObjectIds);
    }

    private Item createMockItem(final String name) {
        Map<String, Object> mockMetadata = new HashMap<>();

        mockMetadata.put("ham", "eggs");
        mockMetadata.put("hamCount", 2000000);  // test non-string insertion/retrieval

        Item item = new Item();
        item.setName(name);
        item.setDisplayName("Foo");
        item.setMetadata(mockMetadata);
        item.setDescription("A Fooable Item.  Don't foo me bro");
        item.setTags(Lists.newArrayList("barkable", "hideable"));
        item.setCategory(FUNGIBLE);
        return item;
    }

    @Test(dataProvider = "casesForTestNormalization")
    public void testNormalization(String caseName, Item toBeSaved, Item expected) {
        Item created = itemDao.createItem(toBeSaved);
        expected.setId(created.getId()); //The expected Item from the data provider will be missing the created Id
        assertEquals(created, expected, "Failed " + caseName);
    }

    @DataProvider(name = "casesForTestNormalization")
    private Object[][] casesForTestNormalization() {
        return new Object[][] {
            {
                "Case: displayName and description are trimmed",
                createItem("test_normalize_0", " displayName\t ", "\tdescription", Lists.newArrayList(), new HashMap<>()),
                createItem("test_normalize_0", "displayName", "description", Lists.newArrayList(), new HashMap<>()),
            },
            {
                "Case: tags are trimmed",
                createItem("test_normalize_1", " displayName ", "\tdescription", Lists.newArrayList(" foo ", "\tbar\t"), new HashMap<>()),
                createItem("test_normalize_1", "displayName", "description", Lists.newArrayList("foo","bar"), new HashMap<>()),
            },
            {
                "Case: tags have inner non alpha-numeric characters, including whitespace, replaced with _",
                createItem("test_normalize_2", " displayName ", "\tdescription", Lists.newArrayList("foo!bar ", "\r\r\t"), new HashMap<>()),
                createItem("test_normalize_2", "displayName", "description", Lists.newArrayList("foo!bar"), new HashMap<>()),
            },
            {
                "Case: tags have upper case characters made lower case",
                createItem("test_normalize_3", " displayName ", "\tdescription", Lists.newArrayList("b\r\r\n\tig   ", "B   ang"), new HashMap<>()),
                createItem("test_normalize_3", "displayName", "description", Lists.newArrayList("big","B_ang"), new HashMap<>()),
            }
        };
    }

    private Item createItem(String name, String displayName, String description, List<String> tags, HashMap<String, Object> metadata) {
        Item item = new Item();
        item.setName(name);
        item.setDisplayName(displayName);
        item.setDescription(description);
        item.setTags(tags);
        item.setMetadata(metadata);
        item.setCategory(FUNGIBLE);
        return item;
    }

    @Test
    private void TestDeleteItem() {

        final var itemName = "to_be_deleted";
        final var item = createMockItem(itemName);
        final var createdItem = itemDao.createItem(item);
        assertNotNull(createdItem.getId());

        itemDao.deleteItem(createdItem.getId());

        var postDelete = itemDao.getItems(0, 100, null, null, null);

        assertTrue(postDelete.stream().noneMatch(i -> i.getId().equals(createdItem.getId())));

        final var recreatedItem = itemDao.createItem(item);

        assertNotNull(createdItem.getName());

        itemDao.deleteItem(createdItem.getName());

        postDelete = itemDao.getItems(0, 100, null, null, null);

        assertTrue(postDelete.stream().noneMatch(i -> i.getId().equals(recreatedItem.getId())));
    }

    @Inject
    public void setItemDao(ItemDao itemDao) {
        this.itemDao = itemDao;
    }

    public Datastore getDatastore() {
        return Datastore;
    }

    @Inject
    public void setDatastore(Datastore Datastore) {
        this.Datastore = Datastore;
    }

    public MatchingMockObjects getMatchingMockObjects() {
        return matchingMockObjects;
    }

    @Inject
    public void setMatchingMockObjects(MatchingMockObjects matchingMockObjects) {
        this.matchingMockObjects = matchingMockObjects;
    }

}
