package com.namazustudios.socialengine.service.schema;

import com.namazustudios.socialengine.dao.MetadataSpecDao;
import com.namazustudios.socialengine.model.Pagination;
import com.namazustudios.socialengine.model.schema.template.CreateMetadataSpecRequest;
import com.namazustudios.socialengine.model.schema.template.MetadataSpec;
import com.namazustudios.socialengine.model.schema.template.UpdateMetadataSpecRequest;
import com.namazustudios.socialengine.model.user.User;
import com.namazustudios.socialengine.service.schema.MetadataSpecService;

import javax.inject.Inject;

public class SuperUserMetadataSpecService implements MetadataSpecService {

    private MetadataSpecDao metadataSpecDao;

    private User user;

    @Override
    public Pagination<MetadataSpec> getMetadataSpecs(
            final int offset,
            final int count) {
        return getTokenTemplateDao().getMetadataSpecs(offset, count);
    }

    @Override
    public MetadataSpec getMetadataSpec(String metadataSpecIdOrName) {
        return getTokenTemplateDao().getMetadataSpec(metadataSpecIdOrName);
    }

    @Override
    public MetadataSpec updateMetadataSpec(String metadataSpecId, UpdateMetadataSpecRequest metadataSpecRequest) {
        return getTokenTemplateDao().updateMetadataSpec(metadataSpecId, metadataSpecRequest);
    }

    @Override
    public MetadataSpec createMetadataSpec(CreateMetadataSpecRequest metadataSpecRequest) {
        return getTokenTemplateDao().createMetadataSpec(metadataSpecRequest);
    }

    @Override
    public void deleteMetadataSpec(String metadataSpecId) {
        getTokenTemplateDao().deleteMetadataSpec(metadataSpecId);
    }

    public User getUser() {
        return user;
    }

    @Inject
    public void setUser(User user) {
        this.user = user;
    }

    public MetadataSpecDao getTokenTemplateDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }
}
