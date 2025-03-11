package dev.getelements.elements.service.schema;

import dev.getelements.elements.sdk.dao.MetadataSpecDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.schema.CreateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.EditorSchema;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;
import dev.getelements.elements.sdk.model.schema.UpdateMetadataSpecRequest;
import dev.getelements.elements.sdk.model.schema.json.JsonSchema;
import dev.getelements.elements.sdk.model.util.MapperRegistry;

import dev.getelements.elements.sdk.service.schema.MetadataSpecService;
import jakarta.inject.Inject;

public class SuperUserMetadataSpecService implements MetadataSpecService {

    private MapperRegistry mapperRegistry;

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

    public MapperRegistry getMapper() {
        return mapperRegistry;
    }

    @Inject
    public void setMapper(MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

}
