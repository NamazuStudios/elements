package dev.getelements.elements.service.formidium;

import dev.getelements.elements.dao.FormidiumInvestorDao;
import dev.getelements.elements.exception.ForbiddenException;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.formidium.FormidiumInvestor;
import dev.getelements.elements.model.user.User;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class UserFormidiumService implements FormidiumService {

    private User user;

    private Client client;

    private FormidiumInvestorDao formidiumInvestorDao;

    private SuperuserFormidiumService superuserFormidiumService;

    @Override
    public FormidiumInvestor createFormidiumInvestor(final String userId,
                                                     final String userAgent,
                                                     final List<Map<String, Object>> multipartFormData) {

        if (userId != null && !Objects.equals(getUser().getId(), userId)) {
            throw new ForbiddenException("Invalid user id: " + userId);
        }

        return getSuperuserFormidiumService().createFormidiumInvestor(getUser().getId(), userAgent, multipartFormData);

    }

    @Override
    public Pagination<FormidiumInvestor> getFormidiumInvestors(final String userId, final int offset, final int count) {
        return userId != null && !Objects.equals(getUser().getId(), userId)
                ? Pagination.empty()
                : getFormidiumUserDao().getFormidiumInvestors(getUser().getId(), offset, count);
    }

    @Override
    public FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId) {
        return getFormidiumUserDao().getFormidiumInvestor(formidiumInvestorId, getUser().getId());
    }

    @Override
    public void deleteFormidiumInvestor(final String formidiumInvestorId) {
        throw new ForbiddenException();
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

    public FormidiumInvestorDao getFormidiumUserDao() {
        return formidiumInvestorDao;
    }

    @Inject
    public void setFormidiumUserDao(FormidiumInvestorDao formidiumInvestorDao) {
        this.formidiumInvestorDao = formidiumInvestorDao;
    }

    public SuperuserFormidiumService getSuperuserFormidiumService() {
        return superuserFormidiumService;
    }

    @Inject
    public void setSuperuserFormidiumService(SuperuserFormidiumService superuserFormidiumService) {
        this.superuserFormidiumService = superuserFormidiumService;
    }

}
