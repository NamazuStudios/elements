package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Form;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class SuperuserFormidiumService implements FormidiumService {

    private Client client;

    private ValidationHelper validationHelper;

    @Override
    public FormidiumInvestor createFormidiumInvestor(final List<Map<String, Object>> multipartFormData) {
        return null;
    }

    @Override
    public Pagination<FormidiumInvestor> getFormidiumInvestors(final String userId, final int offset, final int count) {
        return null;
    }

    @Override
    public FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId) {
        return null;
    }

    @Override
    public void deleteFormidiumInvestor(String formidiumInvestorId) {

    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public ValidationHelper getValidationHelper() {
        return validationHelper;
    }

    @Inject
    public void setValidationHelper(ValidationHelper validationHelper) {
        this.validationHelper = validationHelper;
    }

}
