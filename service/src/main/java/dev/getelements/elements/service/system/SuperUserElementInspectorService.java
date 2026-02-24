package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.ElementArtifactLoader;
import dev.getelements.elements.sdk.ElementPathLoader;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException;
import dev.getelements.elements.sdk.exception.SdkException;
import dev.getelements.elements.sdk.model.exception.InvalidDataException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
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
import java.nio.file.ProviderNotFoundException;
import java.util.List;
import dev.getelements.elements.sdk.record.Artifact;

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
            } catch (ProviderNotFoundException ex) {
                throw new InvalidDataException("Uploaded file is not a valid ELM distribution.", ex);
            }

        } finally {
            Files.deleteIfExists(tempFile);
        }

    }

    @Override
    public List<ElementPathRecordMetadata> inspectElementArtifact(final String coordinates) {

        final Artifact artifact;

        try {
            artifact = elementArtifactLoader.getArtifact(DEFAULTS, coordinates);
        } catch (SdkArtifactNotFoundException ex) {
            throw new NotFoundException("Artifact not found: " + coordinates, ex);
        }

        try (final var fs = FileSystems.newFileSystem(artifact.path())) {
            final var root = fs.getPath("/");
            return elementPathLoader.readElementPaths(root)
                    .map(r -> getMapperRegistry().map(r, ElementPathRecordMetadata.class))
                    .toList();
        } catch (IOException | ProviderNotFoundException ex) {
            throw new InvalidDataException("Artifact is not a valid ELM distribution: " + coordinates, ex);
        }

    }

    @Override
    public List<ElementPathRecordMetadata> inspectElementLargeObject(final String largeObjectId) {
        try (final var is = getLargeObjectBucket().readObject(largeObjectId)) {
            return inspectElement(is);
        } catch (InvalidDataException ex) {
            throw new InvalidDataException("Large object " + largeObjectId + " is not a valid ELM distribution.", ex);
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