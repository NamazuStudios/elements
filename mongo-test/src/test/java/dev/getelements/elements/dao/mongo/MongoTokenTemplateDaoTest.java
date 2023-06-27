package dev.getelements.elements.dao.mongo;

import dev.getelements.elements.BlockchainConstants;
import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.dao.NeoSmartContractDao;
import dev.getelements.elements.dao.TokenTemplateDao;
import dev.getelements.elements.exception.DuplicateException;
import dev.getelements.elements.exception.NotFoundException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.blockchain.ElementsSmartContract;
import dev.getelements.elements.model.blockchain.PatchSmartContractRequest;
import dev.getelements.elements.model.schema.template.*;
import dev.getelements.elements.model.user.User;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import javax.inject.Inject;
import java.util.*;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotEquals;

@Guice(modules = IntegrationTestModule.class)
public class MongoTokenTemplateDaoTest {

    private TokenTemplateDao tokenTemplateDao;

    private MetadataSpecDao metadataSpecDao;

    private NeoSmartContractDao contractDao;

    private UserTestFactory userTestFactory;

    private  String specId;

    private String contractId;

    private String userId;

    @BeforeClass
    public void setupTestItems() {
        Pagination<MetadataSpec> specPagination = getMetadataSpecDao().getMetadataSpecs(0,1);
        List<MetadataSpec> specs = specPagination!=null?specPagination.getObjects():null;
        MetadataSpec spec = specs != null && specs.size() > 0? specs.get(0):null;

        if (spec == null) {
            final var request = new CreateMetadataSpecRequest();
            List<TemplateTab> tabs = new ArrayList<>();
            Map<String, TemplateTabField> fields = new HashMap<>();
            TemplateTabField field = new TemplateTabField();
            field.setName("fieldName");
            fields.put("field1", field);
            field.setFieldType(BlockchainConstants.TemplateFieldType.String);
            request.setTabs(tabs);
            request.setName("New MetadataSpec " + (new Date()).getTime());

            spec = getMetadataSpecDao().createMetadataSpec(request);
        }
        specId = spec.getId();

        Pagination<ElementsSmartContract> contractPagination = getContractDao().getNeoSmartContracts(0,1, "");
        List<ElementsSmartContract> contracts = contractPagination!= null? contractPagination.getObjects():null;
        ElementsSmartContract contract = contracts != null && contracts.size() > 0? contracts.get(0):null;

        if (contract == null){
            final var request = new PatchSmartContractRequest();
            request.setDisplayName("smartcontract-daotest");
            request.setScriptHash("1");
            request.setBlockchain("NEO");

            contract = getContractDao().patchNeoSmartContract(request);
        }
        contractId = contract.getId();

        User user = getUserTestFactory().createTestUser();
        userId = user.getId();

    }

    @Test
    private void testCreateTokenTemplate() {

        final var request = new CreateTokenTemplateRequest();

        String name = "New Token Template " + (new Date()).getTime();
        request.setName(name);
        request.setContractId(this.contractId);
        request.setUserId(this.userId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");

        TokenTemplate inserted = getTokenTemplateDao().createTokenTemplate(request);

        TokenTemplate fetched = getTokenTemplateDao().getTokenTemplate(inserted.getId(), userId);
        assertEquals(name, fetched.getName());
        assertEquals("Token Template", fetched.getDisplayName());

        final Pagination<TokenTemplate> items = getTokenTemplateDao().getTokenTemplates(0, 20, userId);

        assertNotEquals(items.getTotal(), 0);

    }

    @Test(dependsOnMethods = "testCreateTokenTemplate")
    public void testUpdateTokenTemplate() {

        final Pagination<TokenTemplate> items = getTokenTemplateDao().getTokenTemplates(0, 1, userId);

        final var tokenTemplate = items.iterator().next();
        final var idTokenTemplate = getTokenTemplateDao().getTokenTemplate(tokenTemplate.getId(), userId);
        assertEquals(tokenTemplate.getName(), idTokenTemplate.getName());
        assertEquals(tokenTemplate.getId(), idTokenTemplate.getId());

        tokenTemplate.setDisplayName("Updated-Token Template");
        UpdateTokenTemplateRequest updateRequest = new UpdateTokenTemplateRequest();
        String name = "New Token Template " + (new Date()).getTime();
        updateRequest.setName(name);
        updateRequest.setContractId(this.contractId);
        updateRequest.setUserId(this.userId);
        updateRequest.setMetadataSpecId(this.specId);
        updateRequest.setDisplayName("Updated-Token Template");

        final TokenTemplate updatedTemplate = getTokenTemplateDao().updateTokenTemplate(tokenTemplate.getId(), updateRequest);

        assertEquals(updatedTemplate.getId(), tokenTemplate.getId());
        assertEquals(updatedTemplate.getName(), updateRequest.getName());
        assertEquals(updatedTemplate.getDisplayName(), updateRequest.getDisplayName());

    }

    @Test(dependsOnMethods = "testCreateTokenTemplate", expectedExceptions = DuplicateException.class)
    public void testDuplicateTokenTemplateName() {
        final Pagination<TokenTemplate> items = getTokenTemplateDao().getTokenTemplates(0, 1, userId);
        final var tokenTemplate = items.iterator().next();
        String name = tokenTemplate.getName();
        final var request = new CreateTokenTemplateRequest();
        request.setName(name);
        request.setContractId(this.contractId);
        request.setUserId(this.userId);
        request.setMetadataSpecId(this.specId);
        request.setDisplayName("Token Template");

        TokenTemplate inserted = getTokenTemplateDao().createTokenTemplate(request);

    }

    @Test(expectedExceptions = NotFoundException.class)
    public void testTokenTemplateNotFoundById() {
        getTokenTemplateDao().getTokenTemplate("0","0");
    }

    public TokenTemplateDao getTokenTemplateDao() {
        return tokenTemplateDao;
    }

    @Inject
    public void setTokenTemplateDao(TokenTemplateDao tokenTemplateDao) {
        this.tokenTemplateDao = tokenTemplateDao;
    }

    @Inject
    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

    public NeoSmartContractDao getContractDao() {
        return contractDao;
    }

    @Inject
    public void setContractDao(NeoSmartContractDao contractDao) {
        this.contractDao = contractDao;
    }

    public UserTestFactory getUserTestFactory() {
        return userTestFactory;
    }
    @Inject
    public void setUserTestFactory(UserTestFactory userTestFactory) {
        this.userTestFactory = userTestFactory;
    }
}
