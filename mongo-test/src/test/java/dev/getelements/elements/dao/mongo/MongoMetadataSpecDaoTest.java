package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

import static dev.getelements.elements.model.schema.MetadataSpecPropertyType.ENUM;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMetadataSpecDaoTest {

    private MetadataSpecDao metadataSpecDao;

    private String name;

    private String tabName;

    private Integer tabOrder;

    private String fieldName;

    @BeforeClass
    public void setupTestItems() {
        this.tabName = "Tab1";
        this.fieldName="Field1";
        this.tabOrder = 1;
    }

    @DataProvider
    public static Object[][] getFieldType() {
        return Stream
                .of(MetadataSpecPropertyType.values())
                .map(s -> new Object[] {s})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getFieldType")
    public void testCreateMetadataSpec(final MetadataSpecPropertyType fieldType) {
        this.name = "New MetadataSpec " + (new Date()).getTime() + randomUUID().toString();
        testCreateMetadataSpec(name, tabOrder, tabName, fieldName, fieldType);
    }

    private void testCreateMetadataSpec(final String name, final Integer tabOrder, final String tabName, final String fieldName, final MetadataSpecPropertyType fieldType) {
        final var request = new CreateMetadataSpecRequest();
        List<MetadataSpecProperty> properties = new ArrayList<>();
        MetadataSpecProperty property = new MetadataSpecProperty();
        property.setType(fieldType);
        property.setName(fieldName);
        properties.add(property);
        request.setProperties(properties);
        request.setName(name);

        MetadataSpec inserted = getMetadataSpecDao().createMetadataSpec(request);

        MetadataSpec fetched = getMetadataSpecDao().getMetadataSpec(inserted.getId());
        assertEquals(name, fetched.getName());
        MetadataSpecProperty fetchedField = fetched.getProperties().get(0);
        String fetchedFieldName = fetchedField.getName();
        assertEquals(fieldName, fetchedFieldName);
        assertEquals(fieldType, fetchedField.getType());

    }

    @Test(dependsOnMethods = "testCreateMetadataSpec")
    public void testUpdateMetadataSpec() {

        final Pagination<MetadataSpec> items = getMetadataSpecDao().getMetadataSpecs(0, 1);

        final var metadataSpec = items.iterator().next();
        final var idMetadataSpec = getMetadataSpecDao().getMetadataSpec(metadataSpec.getId());
        assertEquals(metadataSpec.getId(), idMetadataSpec.getId());

        UpdateMetadataSpecRequest updateRequest = new UpdateMetadataSpecRequest();
        List<MetadataSpecProperty> properties = new ArrayList<>();

        MetadataSpecProperty property = new MetadataSpecProperty();
        property.setName("Field2");
        property.setType(ENUM);
        properties.add(property);
        TemplateTab tab = new TemplateTab("Tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);

        this.name = "New MetadataSpec " + (new Date()).getTime();;
        updateRequest.setName(name);
        updateRequest.setProperties(tabs);

        final MetadataSpec updatedTemplate = getMetadataSpecDao().updateMetadataSpec(metadataSpec.getId(), updateRequest);

        assertEquals(updatedTemplate.getId(), metadataSpec.getId());
        assertEquals(updatedTemplate.getName(), updateRequest.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());
        assertEquals(updatedTemplate.getTabs().get(0).getFields().get("field2").getFieldType(), ENUM);

        tabs = new ArrayList<>() ;
        tab = new TemplateTab("Tab3",fields);
        tabs.add(tab);
        updateRequest.setProperties(tabs);
        final MetadataSpec updatedTemplate2 = getMetadataSpecDao().updateMetadataSpec(metadataSpec.getId(), updateRequest);

        assertEquals(updatedTemplate2.getId(), metadataSpec.getId());
        assertEquals(updatedTemplate2.getTabs().get(0).getName(), "Tab3");

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testMetadataSpecNotFoundById() {
        getMetadataSpecDao().getMetadataSpec("0");
    }

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }
}
