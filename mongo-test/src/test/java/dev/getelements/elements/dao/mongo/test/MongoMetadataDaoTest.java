package dev.getelements.elements.dao.mongo.test;

import com.fasterxml.jackson.databind.ObjectMapper;
import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.sdk.model.exception.metadata.MetadataNotFoundException;
import dev.getelements.elements.sdk.model.goods.Item;
import dev.getelements.elements.sdk.model.goods.ItemCategory;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MetadataSpecBuilder;
import org.bson.types.ObjectId;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import jakarta.inject.Inject;

import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import static dev.getelements.elements.sdk.model.schema.MetadataSpecPropertyType.*;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMetadataDaoTest {

    private MetadataSpec testMetadataSpec;

    private MetadataDao metadataDao;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    private final List<Metadata> metadataList = new CopyOnWriteArrayList<>();

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
        metadata.setSpec(testMetadataSpec);
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

    }

    @Test(groups = "update", dependsOnGroups = "create", dataProvider = "intermediateMetadataDataProvider")
    public void testUpdateMetadata(final Metadata metadata) {

        final var metadataUpdate = new Metadata();
        metadataUpdate.setId(metadata.getId());
        metadataUpdate.setSpec(metadata.getSpec());
        metadataUpdate.setAccessLevel(metadata.getAccessLevel());
        metadataUpdate.setName("dkfhasakdsfhaks");

        final var map = metadata.getMetadata();
        map.put("UpdateKey", "value7");

        metadataUpdate.setMetadata(metadata.getMetadata());

        final var updatedMetadata = getMetadataDao().updateMetadata(metadataUpdate);

        //Name shouldn't be updatable
        assertNotEquals(updatedMetadata.getName(), metadataUpdate.getName());
        assertEquals(updatedMetadata.getAccessLevel(), metadataUpdate.getAccessLevel());
        assertEquals(updatedMetadata.getMetadata().size(), metadataUpdate.getMetadata().size());
        assertEquals(updatedMetadata.getMetadata().get("UpdateKey"), metadataUpdate.getMetadata().get("UpdateKey"));
    }

    @Test(groups = "fetch", dependsOnGroups = "update", dataProvider = "intermediateMetadataDataProvider")
    public void testGetSingle(final Metadata metadata) {
        final var fetched = getMetadataDao().getMetadata(metadata.getId(), metadata.getAccessLevel());
        final var deserializedItem = fetched.getMetadata().get("ItemKey");
        final var mapper = new ObjectMapper();
        final var serializedItem =  mapper.convertValue(deserializedItem, Item.class);
        fetched.getMetadata().put("ItemKey", serializedItem);

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

    @Test(groups = "delete", dependsOnGroups = "fetch", dataProvider = "intermediateMetadataDataProvider")
    public void testDelete(final Metadata metadata) {
        getMetadataDao().softDeleteMetadata(metadata.getId());
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
