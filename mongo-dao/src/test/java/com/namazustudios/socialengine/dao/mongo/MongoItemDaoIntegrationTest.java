package com.namazustudios.socialengine.dao.mongo;

import com.google.common.collect.Sets;
import com.namazustudios.socialengine.dao.ItemDao;
import com.namazustudios.socialengine.dao.mongo.model.goods.MongoItem;
import com.namazustudios.socialengine.exception.DuplicateException;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.goods.Item;
import org.apache.commons.lang3.RandomStringUtils;
import org.bson.types.ObjectId;
import org.mongodb.morphia.AdvancedDatastore;
import org.testng.annotations.AfterSuite;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;

import java.util.HashMap;
import java.util.HashSet;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;
import static org.testng.Assert.fail;

@Guice(modules = IntegrationTestModule.class)
public class MongoItemDaoIntegrationTest {

    private ItemDao itemDao;

    private EmbeddedMongo embeddedMongo;

    private AdvancedDatastore advancedDatastore;

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
        HashSet<String> tagsToFilterBy = Sets.newHashSet("laser", "tag");

        //Add Items to collection, and track the objectIds that contain the tags we care about.
        Set<ObjectId> expectedObjectIds = new HashSet<>();
        for (int i = 0; i < numberOfItems; i++) {
            Item item = createMockItem();
            HashSet<String> tagsForItem = new HashSet<>();
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

        Item item = new Item();
        item.setName("foo_item");
        item.setDisplayName("Foo");
        item.setMetadata(mockMetadata);
        item.setDescription("A Fooable Item.  Don't foo me bro");
        item.setTags(Sets.newHashSet("barkable", "hideable"));
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
                createItem("name", " displayName\t ", "\tdescription", Sets.newHashSet(), new HashMap<>()),
                createItem("name", "displayName", "description", Sets.newHashSet(), new HashMap<>()),
            },
            {
                "Case: tags are trimmed",
                createItem("name", " displayName ", "\tdescription", Sets.newHashSet(" foo ", "\tbar\t"), new HashMap<>()),
                createItem("name", "displayName", "description", Sets.newHashSet("foo","bar"), new HashMap<>()),
            },
            {
                "Case: tags have inner non alpha-numeric characters, including whitespace, replaced with _",
                createItem("name", " displayName ", "\tdescription", Sets.newHashSet("foo!bar ", "∆delta"), new HashMap<>()),
                createItem("name", "displayName", "description", Sets.newHashSet("foo_bar","_delta"), new HashMap<>()),
            },
            {
                "Case: tags have upper case characters made lower case",
                createItem("name", " displayName ", "\tdescription", Sets.newHashSet("BIG", "BANG"), new HashMap<>()),
                createItem("name", "displayName", "description", Sets.newHashSet("big","bang"), new HashMap<>()),
            }
        };
    }

    private Item createItem(String name, String displayName, String description, HashSet<String> tags, HashMap<String, Object> metadata) {
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

    public EmbeddedMongo getEmbeddedMongo() {
        return embeddedMongo;
    }

    @Inject
    public void setEmbeddedMongo(EmbeddedMongo embeddedMongo) {
        this.embeddedMongo = embeddedMongo;
    }

    public AdvancedDatastore getAdvancedDatastore() {
        return advancedDatastore;
    }

    @Inject
    public void setAdvancedDatastore(AdvancedDatastore advancedDatastore) {
        this.advancedDatastore = advancedDatastore;
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
        advancedDatastore.delete(advancedDatastore.createQuery(MongoItem.class));
    }

    @AfterSuite
    public void killProcess() {
        getEmbeddedMongo().stop();
    }
}
