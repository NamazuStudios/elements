package com.namazustudios.socialengine.dao.mongo;

import com.namazustudios.socialengine.BlockchainConstants;
import com.namazustudios.socialengine.dao.TokenTemplateDao;
import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.template.*;
import com.namazustudios.socialengine.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import static org.testng.Assert.*;

@Guice(modules = IntegrationTestModule.class)
public class MongoTokenTemplateDaoTest {

    private TokenTemplateDao tokenTemplateDao;

    private String tokenName;

    private String contractId;

    private String tabName;

    private Integer tabOrder;

    private String fieldName;

    private String content;

    @BeforeClass
    public void setupTestItems() {
        this.tabName = "Tab1";
        this.fieldName="Field1";
        this.content="New content";
        this.tokenName = "New Token";
        this.contractId = "uu1234";
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
        testCreateTokenTemplate(tokenName, contractId, tabOrder, tabName, fieldName, content, fieldType);
    }

    private void testCreateTokenTemplate(final String tokenName, final String contractId, final Integer tabOrder, final String tabName, final String fieldName, final String content, final BlockchainConstants.TemplateFieldType fieldType) {

        final var request = new CreateTokenTemplateRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName(fieldName);
        field.setContent(content);
        fields.add(field);
        field.setFieldType(fieldType);
        TemplateTab tab = new TemplateTab(tabName,fields);
        tab.setTabOrder(tabOrder);
        tabs.add(tab);
        request.setTabs(tabs);
        request.setTokenName(tokenName);
        request.setContractId(contractId);

        TokenTemplate inserted = getTokenTemplateDao().createTokenTemplate(request);

        TokenTemplate fetched = getTokenTemplateDao().getTokenTemplate(inserted.getId());
        assertEquals(tokenName, fetched.getTokenName());
        assertEquals(contractId, fetched.getContractId());
        assertEquals(tabName, fetched.getTabs().get(0).getName());
        assertEquals(tabOrder, fetched.getTabs().get(0).getTabOrder());
        assertEquals(fieldName, fetched.getTabs().get(0).getFields().get(0).getName());
        assertEquals(content, fetched.getTabs().get(0).getFields().get(0).getContent());
        assertEquals(fieldType, fetched.getTabs().get(0).getFields().get(0).getFieldType());

        final Pagination<TokenTemplate> items = getTokenTemplateDao().getTokenTemplates(0, 20);

        assertNotEquals(items.getTotal(), 0);

        items.forEach(ii -> {

            assertEquals(ii.getTabs().get(0).getFields().get(0).getName(), tab.getFields().get(0).getName());
            assertEquals(ii.getTabs().get(0).getName(), tab.getName());
        });

    }

    @Test(dependsOnMethods = "testCreateTokenTemplate")
    public void testUpdateTokenTemplate() {

        final Pagination<TokenTemplate> items = getTokenTemplateDao().getTokenTemplates(0, 1);

        final var tokenTemplate = items.iterator().next();
        final var idTokenTemplate = getTokenTemplateDao().getTokenTemplate(tokenTemplate.getId());
        assertEquals(tokenTemplate.getTabs().get(0).getFields().get(0).getFieldType(), idTokenTemplate.getTabs().get(0).getFields().get(0).getFieldType());
        assertEquals(tokenTemplate.getId(), idTokenTemplate.getId());

        tokenTemplate.getTabs().get(0).setName("Tab 2");
        UpdateTokenTemplateRequest updateRequest = new UpdateTokenTemplateRequest();
        List<TemplateTab> tabs = new ArrayList<>() ;
        List<TemplateTabField> fields = new ArrayList<>();
        tabs = new ArrayList<>() ;
        fields = new ArrayList<>();
        TemplateTabField field = new TemplateTabField();
        field.setName("Field2");
        field.setContent("Updated Content");
        fields.add(field);
        TemplateTab tab = new TemplateTab("Tab2",fields);
        tab.setTabOrder(2);
        tabs.add(tab);

        updateRequest.setTokenName("Updated Token Name");
        updateRequest.setContractId("uu6789");
        updateRequest.setTabs(tabs);

        final TokenTemplate updatedTemplate = getTokenTemplateDao().updateTokenTemplate(tokenTemplate.getId(), updateRequest);

        assertEquals(updatedTemplate.getId(), tokenTemplate.getId());
        assertEquals(updatedTemplate.getTokenName(), updateRequest.getTokenName());
        assertEquals(updatedTemplate.getContractId(), updateRequest.getContractId());
        assertEquals(updatedTemplate.getTabs().get(0).getName(), tab.getName());
        assertEquals(updatedTemplate.getTabs().get(0).getTabOrder(), tab.getTabOrder());

        tabs = new ArrayList<>() ;
        tab = new TemplateTab("Tab3",fields);
        tabs.add(tab);
        updateRequest.setTabs(tabs);
        final TokenTemplate updatedTemplate2 = getTokenTemplateDao().updateTokenTemplate(tokenTemplate.getId(), updateRequest);

        assertEquals(updatedTemplate2.getId(), tokenTemplate.getId());
        assertEquals(updatedTemplate2.getTabs().get(0).getName(), "Tab3");

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testTokenTemplateNotFoundById() {
        getTokenTemplateDao().getTokenTemplate("0");
    }

    public TokenTemplateDao getTokenTemplateDao() {
        return tokenTemplateDao;
    }

    @Inject
    public void setTokenTemplateDao(TokenTemplateDao tokenTemplateDao) {
        this.tokenTemplateDao = tokenTemplateDao;
    }
}
