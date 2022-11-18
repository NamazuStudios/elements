package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.model.user.User;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;

public class UserFormidiumService implements FormidiumService {

    private User user;

    private Client client;

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
    public void deleteFormidiumInvestor(final String formidiumInvestorId) {

    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

}
