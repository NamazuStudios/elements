package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.system.ElementPathRecordMetadata;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.system.ElementInspectorService;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import jakarta.inject.Inject;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.util.List;

import static dev.getelements.elements.sdk.ElementPathLoader.ELM_EXTENSION;
import static dev.getelements.elements.sdk.record.ArtifactRepository.DEFAULTS;

public class SuperUserElementInspectorService implements ElementInspectorService {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(SuperUserElementInspectorService.class);

    private static final ElementPathLoader elementPathLoader = ElementPathLoader.newDefaultInstance();

    private static final ElementArtifactLoader elementArtifactLoader = ElementArtifactLoader.newDefaultInstance();

    private MapperRegistry mapperRegistry;

    private LargeObjectBucket largeObjectBucket;

    @Override
    public List<ElementPathRecordMetadata> inspectElement(final InputStream inputStream) throws IOException {

        final var tempFile = temporaryFiles.createTempFile(".%s".formatted(ELM_EXTENSION));

        try {

            try (final OutputStream out = Files.newOutputStream(tempFile)) {
                inputStream.transferTo(out);
            }

            try (final var fs = FileSystems.newFileSystem(tempFile)) {
                 final var root = fs.getPath("/");
                return elementPathLoader.readElementPaths(root)
                        .map(r -> getMapperRegistry().map(r, ElementPathRecordMetadata.class))
                        .toList();
            }

        } finally {
            Files.deleteIfExists(tempFile);
        }

    }

    @Override
    public List<ElementPathRecordMetadata> inspectElementArtifact(final String coordinates) {

        final var artifact = elementArtifactLoader.getArtifact(DEFAULTS, coordinates);

        try (final var fs = FileSystems.newFileSystem(artifact.path())) {
             final var root = fs.getPath("/");
             return elementPathLoader.readElementPaths(root)
                     .map(r -> getMapperRegistry().map(r, ElementPathRecordMetadata.class))
                     .toList();
        } catch (IOException ex) {
            throw new RuntimeException("Failed to inspect artifact: " + coordinates, ex);
        }

    }

    @Override
    public List<ElementPathRecordMetadata> inspectElementLargeObject(final String largeObjectId) {
        try (final var is = getLargeObjectBucket().readObject(largeObjectId)) {
            return inspectElement(is);
        } catch (IOException ex) {
            throw new SdkException("Failed to inspect large object: " + largeObjectId, ex);
        }
    }

    public MapperRegistry getMapperRegistry() {
        return mapperRegistry;
    }

    @Inject
    public void setMapperRegistry(final MapperRegistry mapperRegistry) {
        this.mapperRegistry = mapperRegistry;
    }

    public LargeObjectBucket getLargeObjectBucket() {
        return largeObjectBucket;
    }

    @Inject
    public void setLargeObjectBucket(final LargeObjectBucket largeObjectBucket) {
        this.largeObjectBucket = largeObjectBucket;
    }

}