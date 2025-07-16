package dev.getelements.elements.service.metadata;

import dev.getelements.elements.sdk.dao.MetadataDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.metadata.CreateMetadataRequest;
import dev.getelements.elements.sdk.model.metadata.Metadata;
import dev.getelements.elements.sdk.model.metadata.UpdateMetadataRequest;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.service.metadata.MetadataService;
import jakarta.inject.Inject;

public class UserMetadataService implements MetadataService {

    private MetadataDao metadataDao;

    @Override
    public Metadata getMetadataObject(final String metadataId) {
        return metadataDao.getMetadata(metadataId, User.Level.USER);
    }

    @Override
    public Pagination<Metadata> getMetadataObjects(final int offset, final int count) {
        return metadataDao.getMetadatas(offset, count, User.Level.USER);
    }

    @Override
    public Pagination<Metadata> getMetadataObjects(final int offset, final int count, final String search) {
        return metadataDao.searchMetadatas(offset, count, search, User.Level.USER);
    }

    @Override
    public Metadata createMetadata(final CreateMetadataRequest createMetadataRequest) {
        throw new ForbiddenException();
    }

    @Override
    public Metadata updateMetadata(final String metadataId, final UpdateMetadataRequest updateMetadataRequest) {
        throw new ForbiddenException();
    }

    @Override
    public void softDeleteMetadata(final String metadataId) {
        throw new ForbiddenException();
    }

    public MetadataDao getMetadataDao() {
        return metadataDao;
    }

    @Inject
    public void setMetadataDao(final MetadataDao metadataDao) {
        this.metadataDao = metadataDao;
    }
}
