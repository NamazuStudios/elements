package com.namazustudios.socialengine.service.blockchain;

import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.blockchain.CreateSmartContractTemplateRequest;
import com.namazustudios.socialengine.model.blockchain.UpdateSmartContractTemplateRequest;
import com.namazustudios.socialengine.rt.annotation.Expose;
import com.namazustudios.socialengine.rt.annotation.ExposedBindingAnnotation;
import com.namazustudios.socialengine.rt.annotation.ExposedModuleDefinition;
import com.namazustudios.socialengine.model.blockchain.SmartContractTemplate;
import com.namazustudios.socialengine.service.Unscoped;

/**
 * Manages instances of {@link SmartContractTemplate}.
 *
 * Created by keithhudnall on 9/22/21.
 */
@Expose({
        @ExposedModuleDefinition(value = "namazu.elements.service.blockchain.smartcontracttemplate"),
        @ExposedModuleDefinition(
                value = "namazu.elements.service.blockchain.unscoped.smartcontracttemplate",
                annotation = @ExposedBindingAnnotation(Unscoped.class)
        )
})
public interface SmartContractTemplateService {

        /**
         * Lists all {@link SmartContractTemplate} instances starting with the offset and count.
         *
         * @param offset the offset
         * @param count the count
         * @param applicationNameOrId the application name or ID to use when fetching the profiles
         * @param search
         * @return a {@link Pagination} of {@link SmartContractTemplate} instances
         */
        Pagination<SmartContractTemplate> getSmartContractTemplates(int offset, int count,
                                        String applicationNameOrId, String search);

        /**
         * Lists all {@link SmartContractTemplate} instances, specifying a search query.
         *
         * @param offset
         * @param count
         * @param search
         * @return a {@link Pagination} of {@link SmartContractTemplate} instances
         */
        Pagination<SmartContractTemplate> getSmartContractTemplates(int offset, int count, String search);

        /**
         * Fetches a specific {@link SmartContractTemplate} instance based on ID or name.  If not found, an
         * exception is raised.
         *
         * @param templateIdOrName the profile ID
         * @return the {@link SmartContractTemplate}, never null
         */
        SmartContractTemplate getSmartContractTemplate(String templateIdOrName);

        /**
         * Updates the supplied {@link SmartContractTemplate}.  The
         * {@link UpdateSmartContractTemplateRequest#getNameOrId()} method is used to key the
         * {@link SmartContractTemplate}.
         *
         * @param templateRequest the {@link UpdateSmartContractTemplateRequest} with the information to update
         * @return the {@link SmartContractTemplate} as it was changed by the service.
         */
        SmartContractTemplate updateSmartContractTemplate(UpdateSmartContractTemplateRequest templateRequest);

        /**
         * Creates a new profile.  The ID of the profile, as specified by {@link Profile#getId()},
         * should be null and will be assigned.
         *
         * @param templateRequest the {@link CreateSmartContractTemplateRequest} with the information to create
         * @return the {@link SmartContractTemplate} as it was created by the service.
         */
        SmartContractTemplate createSmartContractTemplate(CreateSmartContractTemplateRequest templateRequest);

        /**
         * Deletes the {@link SmartContractTemplate} with the supplied profile ID.
         *
         * @param templateId the template ID.
         */
        void deleteTemplate(String templateId);

}
