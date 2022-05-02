package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.MetadataSpecDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.*;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoMetadataSpecDaoTest {

    private MetadataSpecDao metadataSpecDao;

    private String name;

    private String tabName;

    private Integer tabOrder;

    private String fieldName;

    private String content;

    @BeforeClass
    public void setupTestItems() {
        this.tabName = "Tab1";
        this.fieldName="Field1";
        this.content="New content";
        this.name = "New Token";
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
    public void testCreateTokenTemplate(final BlockchainConstants.TemplateFieldType fieldType) {
        testCreateTokenTemplate(name, tabOrder, tabName, fieldName, content, fieldType);
    }

    private void testCreateTokenTemplate(final String name, final Integer tabOrder, final String tabName, final String fieldName, final String content, final BlockchainConstants.TemplateFieldType fieldType) {

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

        MetadataSpec inserted = getTokenTemplateDao().createMetadataSpec(request);

        MetadataSpec fetched = getTokenTemplateDao().getMetadataSpec(inserted.getId());
        assertEquals(name, fetched.getName());
        assertEquals(tabName, fetched.getTabs().get(0).getName());
        assertEquals(tabOrder, fetched.getTabs().get(0).getTabOrder());
        assertEquals(fieldName, fetched.getTabs().get(0).getFields().get(0).getName());
        assertEquals(fieldType, fetched.getTabs().get(0).getFields().get(0).getFieldType());

        final Pagination<MetadataSpec> items = getTokenTemplateDao().getMetadataSpecs(0, 20);

        assertNotEquals(items.getTotal(), 0);

        items.forEach(ii -> {

            assertEquals(ii.getTabs().get(0).getFields().get(0).getName(), tab.getFields().get(0).getName());
            assertEquals(ii.getTabs().get(0).getName(), tab.getName());
        });

    }

    @Test(dependsOnMethods = "testCreateTokenTemplate")
    public void testUpdateTokenTemplate() {

        final Pagination<MetadataSpec> items = getTokenTemplateDao().getMetadataSpecs(0, 1);

        final var tokenTemplate = items.iterator().next();
        final var idTokenTemplate = getTokenTemplateDao().getMetadataSpec(tokenTemplate.getId());
        assertEquals(tokenTemplate.getTabs().get(0).getFields().get(0).getFieldType(), idTokenTemplate.getTabs().get(0).getFields().get(0).getFieldType());
        assertEquals(tokenTemplate.getId(), idTokenTemplate.getId());

        tokenTemplate.getTabs().get(0).setName("Tab 2");
        UpdateMetadataSpecRequest updateRequest = new UpdateMetadataSpecRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        Map<String, TemplateTabField> fields = new HashMap<>();
        tabs = new ArrayList<>() ;
        TemplateTabField field = new TemplateTabField();
        field.setName("Field2");
        fields.put("field2", field);
        TemplateTab tab = new TemplateTab("Tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);

        updateRequest.setName("Updated Token Name");
        updateRequest.setTabs(tabs);

        final MetadataSpec updatedTemplate = getTokenTemplateDao().updateMetadataSpec(tokenTemplate.getId(), updateRequest);

        assertEquals(updatedTemplate.getId(), tokenTemplate.getId());
        assertEquals(updatedTemplate.getName(), updateRequest.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        tabs = new ArrayList<>() ;
        tab = new TemplateTab("Tab3",fields);
        tabs.add(tab);
        updateRequest.setTabs(tabs);
        final MetadataSpec updatedTemplate2 = getTokenTemplateDao().updateMetadataSpec(tokenTemplate.getId(), updateRequest);

        assertEquals(updatedTemplate2.getId(), tokenTemplate.getId());
        assertEquals(updatedTemplate2.getTabs().get(0).getName(), "Tab3");

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testTokenTemplateNotFoundById() {
        getTokenTemplateDao().getMetadataSpec("0");
    }

    public MetadataSpecDao getTokenTemplateDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setTokenTemplateDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }
}
