package com.namazustudios.socialengine.service.formidium;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.formidium.FormidiumInvestor;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ModuleDefinition;
import com.namazustudios.socialengine.service.Unscoped;

import java.util.List;
import java.util.Map;

/**
 * Accesses the Formidum API and ensures that users in the Formidium system are kept in sync with the users in the
 * Elements database.
 */
@Expose({
        @ModuleDefinition(value = "namazu.elements.service.kyc.formidium"),
        @ModuleDefinition(
                value = "namazu.elements.service.unscoped.kyc.formidium",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface FormidiumService {

    /**
     * Creates a Formidium investor by accessing hte Formidium API and updates the User in the database.
     *
     *
     * @param userId
     * @param userAgent
     * @param multipartFormData the formidium investor create request as multipart form data.
     * @return the formidium investor as written to the database
     */
    FormidiumInvestor createFormidiumInvestor(String userId, String userAgent, List<Map<String, Object>> multipartFormData);

    /**
     * Creates a Formidium investor by accessing hte Formidium API and updates the User in the database.
     *
     * @return a {@link Pagination} containing all the formidium investors
     */
    Pagination<FormidiumInvestor> getFormidiumInvestors(String userId, int offset, int count);

    /**
     * Gets a specific Formidium investor based on the formidium investor ID.
     * @param formidiumInvestorId the formidium user ID
     * @return the {@link FormidiumInvestor}
     */
    FormidiumInvestor getFormidiumInvestor(String formidiumInvestorId);


    /**
     * Deletes a specific Formidium investor based on the formidium investor ID.
     *
     * @param formidiumInvestorId the formidium user ID
     *
     */
    void deleteFormidiumInvestor(String formidiumInvestorId);

}
