package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.dao.SmartContractTemplateDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateSmartContractTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.model.blockchain.UpdateSmartContractTemplateRequest;

import javax.inject.Inject;

public class SuperUserSmartContractTemplateService implements SmartContractTemplateService {

    private SmartContractTemplateDao smartContractTemplateDao;

    @Override
    public Pagination<SmartContractTemplate> getSmartContractTemplates(int offset, int count, String applicationNameOrId, String search) {
        return null;
    }

    @Override
    public Pagination<SmartContractTemplate> getSmartContractTemplates(int offset, int count, String search) {
        return null;
    }

    @Override
    public SmartContractTemplate getSmartContractTemplate(String templateIdOrName) {
        return null;
    }

    @Override
    public SmartContractTemplate updateSmartContractTemplate(UpdateSmartContractTemplateRequest templateRequest) {
        return null;
    }

    @Override
    public SmartContractTemplate createSmartContractTemplate(CreateSmartContractTemplateRequest templateRequest) {
        return null;
    }

    @Override
    public void deleteTemplate(String templateId) {

    }

    public SmartContractTemplateDao getSmartContractTemplateDao() {
        return smartContractTemplateDao;
    }

    @Inject
    public void setSmartContractTemplateDao(SmartContractTemplateDao smartContractTemplateDao) {
        this.smartContractTemplateDao = smartContractTemplateDao;
    }
}
