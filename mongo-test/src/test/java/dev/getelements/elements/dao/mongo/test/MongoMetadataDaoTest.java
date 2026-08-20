package dev.getelements.elements.dao.mongo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.sdk.model.exception.metadata.MetadataNotFoundException;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MetadataSpecBuilder;
import jakarta.inject.Named;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static dev.getelements.elements.sdk.dao.MetadataDao.*;
import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMetadataDaoTest {

    private MetadataSpec testMetadataSpec;

    private MetadataDao metadataDao;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    private final List<Metadata> metadataList = new CopyOnWriteArrayList<>();

    private final Set<String> createdEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> updatedEventIds = ConcurrentHashMap.newKeySet();

    private final Set<String> deletedEventIds = ConcurrentHashMap.newKeySet();

    @BeforeClass
    public void setupEventHandlers() {
        elementRegistry.onEvent(ev -> {
            switch (ev.getEventName()) {
                case METADATA_CREATED -> createdEventIds.add(ev.getEventArgument(0, Metadata.class).getId());
                case METADATA_UPDATED -> updatedEventIds.add(ev.getEventArgument(0, Metadata.class).getId());
                case METADATA_DELETED -> deletedEventIds.add(ev.getEventArgument(0, Metadata.class).getId());
            }
        });
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

    @DataProvider
    public User.Level[] accessLevels() {
        return new User.Level[] {
                User.Level.UNPRIVILEGED,
                User.Level.USER,
                User.Level.SUPERUSER
        };
    }

    @DataProvider
    public Object[][] intermediateMetadataDataProvider() {
        return metadataList.stream()
                .map(a -> new Object[]{a})
                .toArray(Object[][]::new);
    }

    @Test(groups = "create", dataProvider = "accessLevels")
    public void testCreateMetadata(final User.Level accessLevel) {

        final var metadata = new Metadata();
        metadata.setMetadataSpec(testMetadataSpec);
        metadata.setName("test_metadata-" + accessLevel.name());
        metadata.setAccessLevel(accessLevel);

        final var item = new Item();
        item.setName("test_item-" + accessLevel.name());
        item.setDescription("description");
        item.setTags(List.of("tag1", "tag2"));
        item.setCategory(ItemCategory.FUNGIBLE);
        item.setMetadata(Map.of());

        metadata.setMetadata(Map.of(
                "ListKey", List.of("value1", "value2", "value3"),
                "StringKey", "value4",
                "IntKey", 5,
                "ItemKey", item
        ));

        final var createdMetadata = getMetadataDao().createMetadata(metadata);

        metadataList.add(createdMetadata);

        assertNotNull(createdMetadata.getId());
        assertEquals(createdMetadata.getName(), metadata.getName());
        assertEquals(createdMetadata.getAccessLevel(), metadata.getAccessLevel());
        assertEquals(createdMetadata.getMetadata().size(), metadata.getMetadata().size());

        assertTrue(createdEventIds.contains(createdMetadata.getId()),
                "Expected METADATA_CREATED event for " + createdMetadata.getId());

    }

    @Test(groups = "update", dependsOnGroups = "create", dataProvider = "intermediateMetadataDataProvider")
    public void testUpdateMetadata(final Metadata metadata) {

        final var metadataUpdate = new Metadata();
        metadataUpdate.setId(metadata.getId());
        metadataUpdate.setMetadataSpec(metadata.getMetadataSpec());
        metadataUpdate.setAccessLevel(metadata.getAccessLevel());
        metadataUpdate.setName(UUID.randomUUID().toString());

        final var map = metadata.getMetadata();
        map.put("UpdateKey", "value7");

        metadataUpdate.setMetadata(metadata.getMetadata());

        final var updatedMetadata = getMetadataDao().updateMetadata(metadataUpdate);

        assertEquals(updatedMetadata.getName(), metadataUpdate.getName());
        assertEquals(updatedMetadata.getAccessLevel(), metadataUpdate.getAccessLevel());
        assertEquals(updatedMetadata.getMetadata().size(), metadataUpdate.getMetadata().size());
        assertEquals(updatedMetadata.getMetadata().get("UpdateKey"), metadataUpdate.getMetadata().get("UpdateKey"));

        assertTrue(updatedEventIds.contains(updatedMetadata.getId()),
                "Expected METADATA_UPDATED event for " + updatedMetadata.getId());

        metadataList.stream().filter(m -> m.getId().equals(updatedMetadata.getId())).findFirst().ifPresent(m -> {
            m.setMetadata(updatedMetadata.getMetadata());
            m.setName(updatedMetadata.getName());
        });
    }

    @Test(groups = "fetch", dependsOnGroups = "update", dataProvider = "intermediateMetadataDataProvider")
    public void testGetSingle(final Metadata metadata) {
        final var fetched = getMetadataDao().getMetadata(metadata.getId(), metadata.getAccessLevel());
        final var deserializedItem = fetched.getMetadata().get("ItemKey");
        final var mapper = new ObjectMapper();
        final var serializedItem =  mapper.convertValue(deserializedItem, Item.class);

        assertTrue(serializedItem.getClass().isAssignableFrom(Item.class));
        assertEquals(fetched, metadata);
    }

    @Test(groups = "fetch", dependsOnGroups = "update", dataProvider = "accessLevels")
    public void testGetMultiple(final User.Level accessLevel) {

        final var metadatas = getMetadataDao().getMetadatas(0, 20, accessLevel);

        switch (accessLevel) {
            //Should get all 3
            case SUPERUSER -> assertEquals(metadatas.getObjects().size(), 3);
            //Should get 2, excluding the superuser
            case USER -> assertEquals(metadatas.getObjects().size(), 2);
            //Should just get the 1 unprivileged
            case UNPRIVILEGED -> assertEquals(metadatas.getObjects().size(), 1);
        }
    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetAll() {

        final var byId = getMetadataDao().getAllMetadatasBySpec(testMetadataSpec.getId());
        final var byName = getMetadataDao().getAllMetadatasBySpec(testMetadataSpec.getName());

        final var expectedIds = metadataList.stream()
                .map(Metadata::getId)
                .collect(java.util.stream.Collectors.toSet());

        final var byIdIds = byId.stream()
                .map(Metadata::getId)
                .collect(java.util.stream.Collectors.toSet());

        final var byNameIds = byName.stream()
                .map(Metadata::getId)
                .collect(java.util.stream.Collectors.toSet());

        assertEquals(byIdIds, expectedIds);
        assertEquals(byNameIds, expectedIds);

    }

    @Test(groups = "delete", dependsOnGroups = "fetch", dataProvider = "intermediateMetadataDataProvider")
    public void testDelete(final Metadata metadata) {
        getMetadataDao().softDeleteMetadata(metadata.getId());
        assertTrue(deletedEventIds.contains(metadata.getId()),
                "Expected METADATA_DELETED event for " + metadata.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = MetadataNotFoundException.class,
            dataProvider = "intermediateMetadataDataProvider")
    public void testDoubleDelete(final Metadata metadata) {
        getMetadataDao().softDeleteMetadata(metadata.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = MetadataNotFoundException.class,
            dataProvider = "intermediateMetadataDataProvider")
    public void testSpecIsDeleted(final Metadata metadata) {
        getMetadataDao().getMetadata(metadata.getId(), User.Level.SUPERUSER);
    }

    @Test(expectedExceptions = MetadataNotFoundException.class)
    public void testMetadataNotFoundById() {
        final var objectId = new ObjectId();
        getMetadataDao().getMetadata(objectId.toString(), User.Level.SUPERUSER);
    }

    // ---- Global metadata (no MetadataSpec) tests ---------------------------------

    @Test(groups = "create.nospec")
    public void testCreateMetadataWithoutSpec() {
        final var metadata = new Metadata();
        metadata.setName("test_metadata_nospec-" + UUID.randomUUID());
        metadata.setAccessLevel(User.Level.USER);
        metadata.setMetadata(Map.of("key", "value"));

        final var created = getMetadataDao().createMetadata(metadata);
        noSpecMetadata = created;

        assertNotNull(created.getId());
        assertNull(created.getMetadataSpec());
        assertEquals(created.getAccessLevel(), User.Level.USER);
    }

    @Test(groups = "update.nospec", dependsOnGroups = "create.nospec")
    public void testChangeAccessLevelWithoutSpec() {
        final var update = new Metadata();
        update.setId(noSpecMetadata.getId());
        update.setName(noSpecMetadata.getName());
        update.setMetadata(noSpecMetadata.getMetadata());
        update.setMetadataSpec(null);
        update.setAccessLevel(User.Level.SUPERUSER);

        final var updated = getMetadataDao().updateMetadata(update);

        assertEquals(updated.getAccessLevel(), User.Level.SUPERUSER);
        assertNull(updated.getMetadataSpec());
    }

    @Test(groups = "delete.nospec", dependsOnGroups = "update.nospec")
    public void testDeleteMetadataWithoutSpec() {
        getMetadataDao().softDeleteMetadata(noSpecMetadata.getId());
    }

    // ---- Access-level change on metadata with a spec ----------------------------

    @Test(groups = "create.withspec.accesslevel")
    public void testCreateMetadataForAccessLevelChange() {
        final var metadata = new Metadata();
        metadata.setMetadataSpec(testMetadataSpec);
        metadata.setName("test_metadata_accesslevel-" + UUID.randomUUID());
        metadata.setAccessLevel(User.Level.USER);
        metadata.setMetadata(Map.of("key", "value"));

        withSpecAccessLevelMetadata = getMetadataDao().createMetadata(metadata);
        assertNotNull(withSpecAccessLevelMetadata.getId());
    }

    @Test(groups = "update.withspec.accesslevel", dependsOnGroups = "create.withspec.accesslevel")
    public void testChangeAccessLevelWithSpec() {
        final var update = new Metadata();
        update.setId(withSpecAccessLevelMetadata.getId());
        update.setName(withSpecAccessLevelMetadata.getName());
        update.setMetadata(withSpecAccessLevelMetadata.getMetadata());
        update.setMetadataSpec(withSpecAccessLevelMetadata.getMetadataSpec());
        update.setAccessLevel(User.Level.SUPERUSER);

        final var updated = getMetadataDao().updateMetadata(update);

        assertEquals(updated.getAccessLevel(), User.Level.SUPERUSER);
        assertNotNull(updated.getMetadataSpec());
        assertEquals(updated.getMetadataSpec().getId(), withSpecAccessLevelMetadata.getMetadataSpec().getId());
    }

    @Test(groups = "delete.withspec.accesslevel", dependsOnGroups = "update.withspec.accesslevel")
    public void testDeleteMetadataForAccessLevelChange() {
        getMetadataDao().softDeleteMetadata(withSpecAccessLevelMetadata.getId());
    }

    private Metadata noSpecMetadata;
    private Metadata withSpecAccessLevelMetadata;

    public MetadataDao getMetadataDao() {
        return metadataDao;
    }

    @Inject
    public void setMetadataDao(MetadataDao metadataDao) {
        this.metadataDao = metadataDao;
    }

    public MetadataSpecTestFactory getMetadataSpecTestFactory() {
        return metadataSpecTestFactory;
    }

    @Inject
    public void setMetadataSpecTestFactory(MetadataSpecTestFactory metadataSpecTestFactory) {
        this.metadataSpecTestFactory = metadataSpecTestFactory;
    }

}
