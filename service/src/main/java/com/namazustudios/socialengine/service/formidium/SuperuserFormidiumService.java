package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.dao.FormidiumUserDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.service.formidium.api.CreateInvestorResponse;
import com.namazustudios.socialengine.service.formidium.api.FormidiumApiResponse;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class SuperuserFormidiumService implements FormidiumService {

    private Client client;

    private String formidiumApiUrl;

    private FormidiumUserDao formidiumUserDao;

    @Override
    public FormidiumInvestor createFormidiumInvestor(final String userId, final List<Map<String, Object>> multipartFormData) {

        final var entity = Entity.entity(multipartFormData, MULTIPART_FORM_DATA);

        final var response = getClient()
            .target(getFormidiumApiUrl())
            .request(MediaType.APPLICATION_JSON_TYPE)
            .post(entity);

        if (!response.hasEntity())
            throw new InternalException("Formidium returned: " + response.getStatus());

        if (!SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            final var formidiumApiResponse = response.readEntity(FormidiumApiResponse.class);
            throw new BadRequestException("Formidium API returned: " + formidiumApiResponse.getMessage());
        }

        final var createInvestorResponse = response.readEntity(CreateInvestorResponse.class);

        if (!"OK".equals(createInvestorResponse.getStatus())) {
            throw new BadRequestException("Formidium returned: " + createInvestorResponse.getMessage());
        }

        final var data = createInvestorResponse.getData();

        if (data == null || data.isEmpty())
            throw new InternalException("Invalid response data from Formidium.");

        final var investorId = data.get(0).getId();

        if (investorId == null)
            throw new InternalException("Invalid investor id data from Formidium.");

        return getFormidiumUserDao().createInvestor(investorId, userId);

    }

    @Override
    public Pagination<FormidiumInvestor> getFormidiumInvestors(final String userId, final int offset, final int count) {
        return getFormidiumUserDao().getFormidiumInvestors(userId, offset, count);
    }

    @Override
    public FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId) {
        return getFormidiumUserDao().getFormidiumInvestor(formidiumInvestorId);
    }

    @Override
    public void deleteFormidiumInvestor(final String formidiumInvestorId) {
        getFormidiumUserDao().deleteFormidiumInvestor(formidiumInvestorId);
    }

    public Client getClient() {
        return client;
    }

    @Inject
    public void setClient(Client client) {
        this.client = client;
    }

    public String getFormidiumApiUrl() {
        return formidiumApiUrl;
    }

    @Inject
    public void setFormidiumApiUrl(String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    public FormidiumUserDao getFormidiumUserDao() {
        return formidiumUserDao;
    }

    @Inject
    public void setFormidiumUserDao(FormidiumUserDao formidiumUserDao) {
        this.formidiumUserDao = formidiumUserDao;
    }

}
