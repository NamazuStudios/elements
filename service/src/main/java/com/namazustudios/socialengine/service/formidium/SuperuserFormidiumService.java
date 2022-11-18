package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.CreateFormidiumInvestorRequest;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.util.ValidationHelper;

import javax.inject.Inject;
import javax.ws.rs.client.Client;
import javax.ws.rs.core.Form;
import java.util.Collection;
import java.util.Objects;

public class SuperuserFormidiumService implements FormidiumService {

    private Client client;

    private ValidationHelper validationHelper;

    @Override
    public FormidiumInvestor createFormidiumInvestor(final CreateFormidiumInvestorRequest createFormidiumInvestorRequest) {

        final var form = new Form();

        getValidationHelper().validateModel(validationHelper);

        for(final var entry : createFormidiumInvestorRequest.getFormidiumApiParameters().entrySet()) {

            final var key = entry.getKey();
            final var value = entry.getValue();

            if (value instanceof Collection<?>) {
                final Collection<?> collection = (Collection<?>) value;
                collection.stream()
                    .filter(Objects::nonNull)
                    .map(Object::toString)
                    .forEach(s -> form.param(key, s));
            } else if (value != null) {
                form.param(key, value.toString());
            }

        }

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
