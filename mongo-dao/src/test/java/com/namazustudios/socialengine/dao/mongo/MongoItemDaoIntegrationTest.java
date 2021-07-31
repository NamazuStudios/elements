package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import dev.morphia.Datastore;
import dev.morphia.DeleteOptions;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.testng.annotations.*;
import org.testng.collections.Lists;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

@Guice(modules = IntegrationTestModule.class)
public class MongoItemDaoIntegrationTest {

    private ItemDao itemDao;

    private Datastore Datastore;

    private MatchingMockObjects matchingMockObjects;

    @Test
    public void testCreateAndRead() {
        Item item = createMockItem();
        Item createdItem = itemDao.createItem(item);
        assertNotNull(createdItem.getId());

        Item byName = itemDao.getItemByIdOrName(createdItem.getName());
        assertEquals(byName, createdItem);
        Item byId = itemDao.getItemByIdOrName(createdItem.getId());
        assertEquals(byId, createdItem);
    }

    @Test
    public void testCreateAndNonStringMetadataValue() {
        Item item = createMockItem();
        Item createdItem = itemDao.createItem(item);
        assertEquals(createdItem.getMetadata().get("hamCount"), Integer.valueOf(2000000));
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetItemByIdNotFound() {
        itemDao.getItemByIdOrName(new ObjectId().toHexString());
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testGetItemByNameNotFound() {
        itemDao.getItemByIdOrName("non_existent_name");
    }

    @Test(expectedExceptions = DuplicateException.class, enabled = false)
    public void testCreateNameAlreadyExists() {
        Item item = createMockItem();
        Item created = itemDao.createItem(item);
        assertNotNull(created);
        itemDao.createItem(item);
    }

    @Test
    public void testModify() {
        Item item = createMockItem();
        Item createdItem = itemDao.createItem(item);
        createdItem.setDisplayName("Different Display Name");

        Item fromUpdate = itemDao.updateItem(createdItem);
        assertEquals(fromUpdate.getDisplayName(), "Different Display Name");

        Item lookedUpItem = itemDao.getItemByIdOrName(createdItem.getId());
        assertEquals(fromUpdate, lookedUpItem);
    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testModifyNotFound() {
        Item item = createMockItem();
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
            Item item = createMockItem();
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
        Pagination<Item> itemPagination = itemDao.getItems(0, Integer.MAX_VALUE, tagsToFilterBy, null);
        while (itemPagination.getOffset() < itemPagination.getTotal()) {
            int nextOffset = itemPagination.getObjects().size() + itemPagination.getOffset();
            seen.addAll(itemPagination.getObjects().stream()
                .map(item -> new ObjectId(item.getId())).collect(Collectors.toList()));
            itemPagination = itemDao.getItems(nextOffset, Integer.MAX_VALUE, tagsToFilterBy, null);
        }

        assertEquals(seen, expectedObjectIds);
    }

    private Item createMockItem() {
        Map<String, Object> mockMetadata = new HashMap<>();
        mockMetadata.put("ham", "eggs");
        mockMetadata.put("hamCount", 2000000);  // test non-string insertion/retrieval

        Item item = new Item();
        item.setName("foo_item");
        item.setDisplayName("Foo");
        item.setMetadata(mockMetadata);
        item.setDescription("A Fooable Item.  Don't foo me bro");
        item.setTags(Lists.newArrayList("barkable", "hideable"));
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
                createItem("name", " displayName\t ", "\tdescription", Lists.newArrayList(), new HashMap<>()),
                createItem("name", "displayName", "description", Lists.newArrayList(), new HashMap<>()),
            },
            {
                "Case: tags are trimmed",
                createItem("name", " displayName ", "\tdescription", Lists.newArrayList(" foo ", "\tbar\t"), new HashMap<>()),
                createItem("name", "displayName", "description", Lists.newArrayList("foo","bar"), new HashMap<>()),
            },
            {
                "Case: tags have inner non alpha-numeric characters, including whitespace, replaced with _",
                createItem("name", " displayName ", "\tdescription", Lists.newArrayList("foo!bar ", "\r\r\t"), new HashMap<>()),
                createItem("name", "displayName", "description", Lists.newArrayList("foo!bar"), new HashMap<>()),
            },
            {
                "Case: tags have upper case characters made lower case",
                createItem("name", " displayName ", "\tdescription", Lists.newArrayList("b\r\r\n\tig   ", "B   ang"), new HashMap<>()),
                createItem("name", "displayName", "description", Lists.newArrayList("big","B_ang"), new HashMap<>()),
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
        return item;
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

    @BeforeMethod
    public void deleteAllItems() {
        Datastore.find(MongoItem.class).delete(new DeleteOptions().multi(true));
    }

    @AfterSuite
    public void killProcess() {
//        getEmbeddedMongo().stop();
    }
}
