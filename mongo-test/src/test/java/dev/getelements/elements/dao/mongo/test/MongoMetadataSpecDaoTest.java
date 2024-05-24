package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.exception.schema.MetadataSpecNotFoundException;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.MetadataSpecProperty;
import dev.getelements.elements.model.schema.MetadataSpecPropertyType;
import dev.getelements.elements.util.PaginationWalker;
import org.bson.types.ObjectId;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.*;
import static java.lang.String.format;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMetadataSpecDaoTest {

    private MetadataSpec working;

    private MetadataSpecDao metadataSpecDao;

    private MetadataSpecTestFactory metadataSpecTestFactory;

    @Test(groups = "create")
    public void testCreateMetadataSpec() {

        final var spec = getMetadataSpecTestFactory().createTestSpecNoInsert("test", s -> {

            final var properties = Stream.of(MetadataSpecPropertyType.values())
                    .map(type -> {

                        final var property = new MetadataSpecProperty();
                        property.setType(type);
                        property.setName(format("%s_field", type.name().toLowerCase()));
                        property.setDisplayName(format("Test field for %s", type.name().toLowerCase()));

                        if (OBJECT.equals(type) || ARRAY.equals(type)) {
                            final var subProperty = new MetadataSpecProperty();
                            subProperty.setType(STRING);
                            subProperty.setName("sub_string_field");
                            subProperty.setDisplayName("Sub-field.");
                            property.setProperties(List.of(subProperty));
                        }

                        return property;

                    })
                    .collect(Collectors.toList());

            s.setType(OBJECT);
            s.setProperties(properties);

            return s;
        });

        working = getMetadataSpecDao().createMetadataSpec(spec);

        assertNotNull(working.getId());
        assertEquals(working.getName(), spec.getName());
        assertEquals(working.getType(), spec.getType());
        assertEquals(working.getProperties(), spec.getProperties());

    }

    @Test(groups = "update", dependsOnGroups = "create")
    public void testUpdateMetadataSpec() {

        final var properties = Stream.of(MetadataSpecPropertyType.values())
                .map(type -> {

                    final var property = new MetadataSpecProperty();
                    property.setType(type);
                    property.setName(format("%s_field_update", type.name().toLowerCase()));
                    property.setDisplayName(format("Test updated field for %s", type.name().toLowerCase()));

                    if (OBJECT.equals(type) || ARRAY.equals(type)) {
                        final var subProperty = new MetadataSpecProperty();
                        subProperty.setType(STRING);
                        subProperty.setName("sub_string_field_update");
                        subProperty.setDisplayName("Sub-field.");
                        property.setProperties(List.of(subProperty));
                    }

                    return property;

                })
                .collect(Collectors.toList());

        working.setProperties(properties);

        final var updated = getMetadataSpecDao().updateActiveMetadataSpec(working);
        assertEquals(working, updated);
        working = updated;

    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetSingle() {
        final var fetched = getMetadataSpecDao().getActiveMetadataSpec(working.getId());
        assertEquals(fetched, working);
    }

    @Test(groups = "fetch", dependsOnGroups = "update")
    public void testGetMultiple() {
        final var specs = new PaginationWalker().toList(getMetadataSpecDao()::getActiveMetadataSpecs);
        assertTrue(specs.contains(working));
    }

    @Test(groups = "delete", dependsOnGroups = "fetch")
    public void testDelete() {
        getMetadataSpecDao().deleteMetadataSpec(working.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = MetadataSpecNotFoundException.class)
    public void testDoubleDelete() {
        getMetadataSpecDao().deleteMetadataSpec(working.getId());
    }

    @Test(groups = "delete",
            dependsOnMethods = "testDelete",
            expectedExceptions = MetadataSpecNotFoundException.class)
    public void testSpecIsDeleted() {
        getMetadataSpecDao().getActiveMetadataSpec(working.getId());
    }

    @Test(expectedExceptions = MetadataSpecNotFoundException.class)
    public void testMetadataSpecNotFoundById() {
        final var objectId = new ObjectId();
        getMetadataSpecDao().getActiveMetadataSpec(objectId.toString());
    }

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

    public MetadataSpecTestFactory getMetadataSpecTestFactory() {
        return metadataSpecTestFactory;
    }

    @Inject
    public void setMetadataSpecTestFactory(MetadataSpecTestFactory metadataSpecTestFactory) {
        this.metadataSpecTestFactory = metadataSpecTestFactory;
    }

}
