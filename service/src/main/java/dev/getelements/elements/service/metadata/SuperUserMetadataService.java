package dev.getelements.elements.service.metadata;

import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.metadata.CreateMetadataRequest;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.metadata.UpdateMetadataRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import jakarta.inject.Inject;

public class SuperUserMetadataService implements MetadataService {

    private MetadataDao metadataDao;

    @Override
    public Metadata getMetadataObject(final String metadataId) {
        return metadataDao.getMetadata(metadataId, User.Level.SUPERUSER);
    }

    @Override
    public Pagination<Metadata> getMetadataObjects(final int offset, final int count) {
        return metadataDao.getMetadatas(offset, count, User.Level.SUPERUSER);
    }

    @Override
    public Pagination<Metadata> getMetadataObjects(final int offset, final int count, final String search) {
        return metadataDao.searchMetadatas(offset, count, search, User.Level.SUPERUSER);
    }

    @Override
    public Metadata createMetadata(final CreateMetadataRequest createMetadataRequest) {

        final var metadata = new Metadata();
        metadata.setMetadata(createMetadataRequest.getMetadata());
        metadata.setName(createMetadataRequest.getName());
        metadata.setMetadataSpec(createMetadataRequest.getMetadataSpec());
        metadata.setAccessLevel(createMetadataRequest.getAccessLevel());

        return metadataDao.createMetadata(metadata);
    }

    @Override
    public Metadata updateMetadata(final String metadataId, final UpdateMetadataRequest updateMetadataRequest) {

        final var metadata = new Metadata();
        metadata.setId(metadataId);
        metadata.setName(updateMetadataRequest.getName());
        metadata.setMetadata(updateMetadataRequest.getMetadata());
        metadata.setMetadataSpec(updateMetadataRequest.getMetadataSpec());
        metadata.setAccessLevel(updateMetadataRequest.getAccessLevel());

        return metadataDao.updateMetadata(metadata);
    }

    @Override
    public void softDeleteMetadata(final String metadataId) {
        metadataDao.softDeleteMetadata(metadataId);
    }

    public MetadataDao getMetadataDao() {
        return metadataDao;
    }

    @Inject
    public void setMetadataDao(final MetadataDao metadataDao) {
        this.metadataDao = metadataDao;
    }
}
