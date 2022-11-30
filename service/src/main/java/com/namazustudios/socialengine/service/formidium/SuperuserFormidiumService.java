package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.dao.FormidiumInvestorDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.rt.exception.BadRequestException;
import com.namazustudios.socialengine.rt.exception.InternalException;
import com.namazustudios.socialengine.service.formidium.api.CreateInvestorResponse;

import javax.inject.Inject;
import javax.inject.Named;
import javax.ws.rs.client.Client;
import javax.ws.rs.client.Entity;
import javax.ws.rs.core.MediaType;
import java.util.List;
import java.util.Map;

import static com.namazustudios.socialengine.service.formidium.FormidiumConstants.*;
import static java.lang.String.format;
import static javax.ws.rs.core.MediaType.MULTIPART_FORM_DATA;
import static javax.ws.rs.core.Response.Status.Family.SUCCESSFUL;

public class SuperuserFormidiumService implements FormidiumService {

    private static final String ADD_INVESTOR_API = "csdapi/v1/csd_investor_api";

    private Client client;

    private String formidiumApiKey;

    private String formidiumApiUrl;

    private FormidiumInvestorDao formidiumInvestorDao;

    @Override
    public FormidiumInvestor createFormidiumInvestor(final String userId,
                                                     final String userAgent,
                                                     final List<Map<String, Object>> multipartFormData) {

        final var entity = Entity.entity(multipartFormData, MULTIPART_FORM_DATA);
        final var url = format("%s/%s", getFormidiumApiUrl(), ADD_INVESTOR_API).replace("/+", "/");

        final var response = getClient()
            .target(url)
            .request(MediaType.APPLICATION_JSON_TYPE)
            .header("User-Agent", "Mozilla/5.0 (Windows NT 6.1; Win64; x64; rv:47.0) Gecko/20100101 Firefox/47.0")
            .header(FORMIDIUM_API_KEY_HEADER, getFormidiumApiKey())
            .post(entity);

        if (!response.hasEntity())
            throw new InternalException("Formidium returned: " + response.getStatus());

        if (!SUCCESSFUL.equals(response.getStatusInfo().getFamily())) {
            final var formidiumApiResponse = response.readEntity(String.class);
            throw new BadRequestException("Formidium API returned: " + formidiumApiResponse);
        }

        final var createInvestorResponse = response.readEntity(CreateInvestorResponse.class);

        if (!"OK".equals(createInvestorResponse.getStatus())) {
            throw new BadRequestException("Formidium returned: " + createInvestorResponse.getMessage());
        }

        final var data = createInvestorResponse.getData();

        if (data == null)
            throw new InternalException("Invalid response data from Formidium.");

        final var investorId = data.getId();

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

    public String getFormidiumApiKey() {
        return formidiumApiKey;
    }

    @Inject
    public void setFormidiumApiKey(@Named(FORMIDIUM_API_KEY) String formidiumApiKey) {
        this.formidiumApiKey = formidiumApiKey;
    }

    public String getFormidiumApiUrl() {
        return formidiumApiUrl;
    }

    @Inject
    public void setFormidiumApiUrl(@Named(FORMIDIUM_API_URL) String formidiumApiUrl) {
        this.formidiumApiUrl = formidiumApiUrl;
    }

    public FormidiumInvestorDao getFormidiumUserDao() {
        return formidiumInvestorDao;
    }

    @Inject
    public void setFormidiumUserDao(FormidiumInvestorDao formidiumInvestorDao) {
        this.formidiumInvestorDao = formidiumInvestorDao;
    }

}
