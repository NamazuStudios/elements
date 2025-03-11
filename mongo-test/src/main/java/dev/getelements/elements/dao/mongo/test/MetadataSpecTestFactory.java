package dev.getelements.elements.dao.mongo.test;

import dev.getelements.elements.sdk.dao.MetadataSpecDao;
import dev.getelements.elements.sdk.model.schema.MetadataSpec;

import jakarta.inject.Inject;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Function;

import static java.lang.String.format;

public class MetadataSpecTestFactory {

    private static final AtomicInteger suffix = new AtomicInteger();

    private MetadataSpecDao metadataSpecDao;

    public MetadataSpec createTestSpec(
            final String name,
            final Function<MetadataSpec, MetadataSpec> metadataSpecTransformer) {
        return createTestSpecNoInsert(name, metadataSpecTransformer.andThen(getMetadataSpecDao()::createMetadataSpec));
    }

    public MetadataSpec createTestSpecNoInsert(
            final String name,
            final Function<MetadataSpec, MetadataSpec> metadataSpecTransformer) {
        final var metadataSpec = new MetadataSpec();
        final var fullyQualifiedName = format("%s%d", name, suffix.getAndIncrement());
        metadataSpec.setName(fullyQualifiedName);
        return metadataSpecTransformer.apply(metadataSpec);
    }

    public MetadataSpecDao getMetadataSpecDao() {
        return metadataSpecDao;
    }

    @Inject
    public void setMetadataSpecDao(MetadataSpecDao metadataSpecDao) {
        this.metadataSpecDao = metadataSpecDao;
    }

}
