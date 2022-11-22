package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;

public interface FormidiumUserDao {

    FormidiumInvestor createInvestor(final String investorId, final String userId);

    Pagination<FormidiumInvestor> getFormidiumInvestors(String userId, int offset, int count);

    default FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId) {
        return getFormidiumInvestor(formidiumInvestorId, null);
    }

    FormidiumInvestor getFormidiumInvestor(String formidiumInvestorId, String userId);

    void deleteFormidiumInvestor(String formidiumInvestorId);

}
