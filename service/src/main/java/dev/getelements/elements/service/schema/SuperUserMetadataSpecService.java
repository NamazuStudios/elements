package dev.getelements.elements.service.schema;

import dev.getelements.elements.dao.MetadataSpecDao;
import dev.getelements.elements.model.Pagination;
import dev.getelements.elements.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.model.schema.EditorSchema;
import dev.getelements.elements.model.schema.MetadataSpec;
import dev.getelements.elements.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.model.schema.json.JsonSchema;
import dev.getelements.elements.model.user.User;
import org.dozer.Mapper;

import javax.inject.Inject;

public class SuperUserMetadataSpecService implements MetadataSpecService {

    private Mapper mapper;

    private MetadataSpecDao metadataSpecDao;

    @Override
    public Pagination<MetadataSpec> getMetadataSpecs(
            final int offset,
            final int count) {
        return getMetadataSpecDao().getActiveMetadataSpecs(offset, count);
    }

    @Override
    public MetadataSpec getMetadataSpec(final String metadataSpecIdOrName) {
        return getMetadataSpecDao().getActiveMetadataSpec(metadataSpecIdOrName);
    }

    @Override
    public JsonSchema getJsonSchema(final String metadataSpecName) {
        final var spec = getMetadataSpecDao().getActiveMetadataSpecByName(metadataSpecName);
        return getMapper().map(spec, JsonSchema.class);
    }

    @Override
    public EditorSchema getEditorSchema(final String metadataSpecName) {
        final var spec = getMetadataSpecDao().getActiveMetadataSpecByName(metadataSpecName);
        return getMapper().map(spec, EditorSchema.class);
    }

    @Override
    public MetadataSpec updateMetadataSpec(final String metadataSpecId,
                                           final UpdateMetadataSpecRequest metadataSpecRequest) {
        final var spec = getMetadataSpecDao().getActiveMetadataSpec(metadataSpecId);
        spec.setName(metadataSpecRequest.getName());
        spec.setType(metadataSpecRequest.getType());
        spec.setProperties(metadataSpecRequest.getProperties());
        return getMetadataSpecDao().updateActiveMetadataSpec(spec);
    }

    @Override
    public MetadataSpec createMetadataSpec(final CreateMetadataSpecRequest metadataSpecRequest) {
        final var spec = new MetadataSpec();
        spec.setName(metadataSpecRequest.getName());
        spec.setType(metadataSpecRequest.getType());
        spec.setProperties(metadataSpecRequest.getProperties());
        return getMetadataSpecDao().createMetadataSpec(spec);
    }

    @Override
    public void deleteMetadataSpec(String metadataSpecId) {
        getMetadataSpecDao().deleteMetadataSpec(metadataSpecId);
    }

    public Mapper getMapper() {
        return mapper;
    }

    @Inject
    public void setMapper(Mapper mapper) {
        this.mapper = mapper;
    }

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

}
