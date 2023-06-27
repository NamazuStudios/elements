package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.template.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Stream;

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
                .of(BlockchainConstants.TemplateFieldType.values())
                .map(s -> new Object[] {s})
                .toArray(Object[][]::new);
    }

    @Test(dataProvider = "getFieldType")
    public void testCreateMetadataSpec(final BlockchainConstants.TemplateFieldType fieldType) {
        this.name = "New MetadataSpec " + (new Date()).getTime() + randomUUID().toString();
        testCreateMetadataSpec(name, tabOrder, tabName, fieldName, fieldType);
    }

    private void testCreateMetadataSpec(final String name, final Integer tabOrder, final String tabName, final String fieldName, final BlockchainConstants.TemplateFieldType fieldType) {
        final var request = new CreateMetadataSpecRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, TemplateTabField> fields = new HashMap<>();
        TemplateTabField field = new TemplateTabField();
        field.setName(fieldName);
        fields.put("field1", field);
        field.setFieldType(fieldType);
        TemplateTab tab = new TemplateTab(tabName,fields);
        tab.setTabOrder(tabOrder);
        tabs.add(tab);
        request.setTabs(tabs);
        request.setName(name);

        MetadataSpec inserted = getMetadataSpecDao().createMetadataSpec(request);

        MetadataSpec fetched = getMetadataSpecDao().getMetadataSpec(inserted.getId());
        assertEquals(name, fetched.getName());
        assertEquals(tabName, fetched.getTabs().get(0).getName());
        assertEquals(tabOrder, fetched.getTabs().get(0).getTabOrder());
        TemplateTabField fetchedField = fetched.getTabs().get(0).getFields().get("field1");
        String fetchedFieldName = fetchedField.getName();
        assertEquals(fieldName, fetchedFieldName);
        assertEquals(fieldType, fetchedField.getFieldType());
    }

    @Test(dependsOnMethods = "testCreateMetadataSpec")
    public void testUpdateMetadataSpec() {

        final Pagination<MetadataSpec> items = getMetadataSpecDao().getMetadataSpecs(0, 1);

        final var metadataSpec = items.iterator().next();
        final var idMetadataSpec = getMetadataSpecDao().getMetadataSpec(metadataSpec.getId());
        assertEquals(metadataSpec.getId(), idMetadataSpec.getId());

        UpdateMetadataSpecRequest updateRequest = new UpdateMetadataSpecRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, TemplateTabField> fields = new HashMap<>();
        tabs = new ArrayList<>() ;
        TemplateTabField field = new TemplateTabField();
        field.setName("Field2");
        field.setFieldType(BlockchainConstants.TemplateFieldType.Enum);
        fields.put("field2", field);
        TemplateTab tab = new TemplateTab("Tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);

        this.name = "New MetadataSpec " + (new Date()).getTime();;
        updateRequest.setName(name);
        updateRequest.setTabs(tabs);

        final MetadataSpec updatedTemplate = getMetadataSpecDao().updateMetadataSpec(metadataSpec.getId(), updateRequest);

        assertEquals(updatedTemplate.getId(), metadataSpec.getId());
        assertEquals(updatedTemplate.getName(), updateRequest.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());
        assertEquals(updatedTemplate.getTabs().get(0).getFields().get("field2").getFieldType(), BlockchainConstants.TemplateFieldType.Enum);

        tabs = new ArrayList<>() ;
        tab = new TemplateTab("Tab3",fields);
        tabs.add(tab);
        updateRequest.setTabs(tabs);
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
