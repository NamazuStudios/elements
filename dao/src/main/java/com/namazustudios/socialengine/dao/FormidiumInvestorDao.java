package com.namazustudios.socialengine.dao;

import com.namazustudios.socialengine.exception.NotFoundException;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;

import java.util.Optional;

public interface FormidiumInvestorDao {

    /**
     * Creates a new formidium investor.
     *
     * @param investorId the investor id as assigned by Formidium
     * @param userId the user id
     * @return the investor as written to the database
     */
    FormidiumInvestor createInvestor(final String investorId, final String userId);

    /**
     * Gets all formidium investors matching a specific user.
     *
     * @param userId the user id
     * @param offset the offset
     * @param count the count
     * @return a {@link Pagination<FormidiumInvestor>}
     */
    Pagination<FormidiumInvestor> getFormidiumInvestors(String userId, int offset, int count);

    /**
     * Finds a {@link FormidiumInvestor} with the supplied id.
     *
     * @param formidiumInvestorId the formidium investor id
     * @return an {@link Optional<FormidiumInvestor>}
     */
    default Optional<FormidiumInvestor> findFormidiumInvestor(final String formidiumInvestorId) {
        return findFormidiumInvestor(formidiumInvestorId, null);
    }

    /**
     * Finds a {@link FormidiumInvestor} with the supplied id that alos matches the supplied user id.
     *
     * @param formidiumInvestorId the formidium investor id
     * @param userId the user id, or null
     * @return an {@link Optional<FormidiumInvestor>}
     */
    Optional<FormidiumInvestor> findFormidiumInvestor(String formidiumInvestorId, String userId);

    /**
     * Gets a {@link FormidiumInvestor} by ID.
     *
     * @param formidiumInvestorId the investor
     * @return the {@link FormidiumInvestor}, never null
     * @throws NotFoundException if the investor is not found.
     */
    default FormidiumInvestor getFormidiumInvestor(final String formidiumInvestorId) {
        return findFormidiumInvestor(formidiumInvestorId).orElseThrow(NotFoundException::new);
    }

    /**
     * Gets a {@link FormidiumInvestor} by ID that optionally matches the user id
     *
     * @param formidiumInvestorId the investor
     * @return the {@link FormidiumInvestor}, never null
     * @param userId the user id, or null
     * @throws NotFoundException if the investor is not found.
     */
    default FormidiumInvestor getFormidiumInvestor(String formidiumInvestorId, String userId) {
        return findFormidiumInvestor(formidiumInvestorId, userId).orElseThrow(NotFoundException::new);
    }

    /**
     * Deletse a {@link FormidiumInvestor} with the supplied id
     *
     * @param formidiumInvestorId the formidium investor id
     */
    void deleteFormidiumInvestor(String formidiumInvestorId);

}
