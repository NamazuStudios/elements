package dev.getelements.elements.sdk.service.system;

import dev.getelements.elements.sdk.annotation.ElementPublic;
import dev.getelements.elements.sdk.annotation.ElementServiceExport;
import dev.getelements.elements.sdk.model.system.ElementPathRecordMetadata;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import static dev.getelements.elements.sdk.service.Constants.UNSCOPED;

/**
 * Inspects an Element distribution (ELM file) and returns structural metadata describing its contents. The returned
 * {@link ElementPathRecordMetadata} describes the paths to all jars and classpath entries found within the
 * distribution, along with the element's {@code manifest} and {@code attributes}.
 *
 * <p>All three methods inspect the same logical artifact — an ELM file — sourced from different locations:
 * a raw stream, a Maven artifact repository, or the large-object store.</p>
 */
@ElementPublic
@ElementServiceExport
@ElementServiceExport(name = UNSCOPED)
public interface ElementInspectorService {

    /**
     * Inspects an ELM distribution read from the supplied {@link InputStream}. The stream is expected to contain a
     * valid ELM file (a zip-format archive). The stream is consumed but not closed by this method; the caller retains
     * responsibility for closing it.
     *
     * @param inputStream the stream containing the ELM file contents; must not be {@code null}
     * @return a {@link ElementPathRecordMetadata} describing the first element found within the distribution
     * @throws IOException if there was an error reading from the supplied stream or writing to the temporary file
     * @throws dev.getelements.elements.sdk.exception.SdkElementNotFoundException if the archive contains no
     *         recognisable element directories
     */
    List<ElementPathRecordMetadata> inspectElement(InputStream inputStream) throws IOException;

    /**
     * Inspects an ELM distribution resolved from Maven artifact coordinates. The coordinates must be in standard
     * Maven format ({@code groupId:artifactId:version} or {@code groupId:artifactId:packaging:version}). The artifact
     * is resolved against the default repositories (Maven Central) and must be an ELM file.
     *
     * @param coordinates the Maven artifact coordinates identifying the ELM distribution; must not be {@code null}
     * @return a {@link ElementPathRecordMetadata} describing the first element found within the distribution
     * @throws dev.getelements.elements.sdk.exception.SdkArtifactNotFoundException if the artifact cannot be resolved
     * @throws dev.getelements.elements.sdk.exception.SdkElementNotFoundException if the artifact contains no
     *         recognisable element directories
     */
    List<ElementPathRecordMetadata> inspectElementArtifact(String coordinates);

    /**
     * Inspects an ELM distribution previously stored in the large-object store. The {@code largeObjectId} must refer
     * to an existing large object whose content is a valid ELM file.
     *
     * @param largeObjectId the id of the large object containing the ELM file; must not be {@code null}
     * @return a {@link ElementPathRecordMetadata} describing the first element found within the distribution
     * @throws dev.getelements.elements.sdk.exception.SdkElementNotFoundException if the large object contains no
     *         recognisable element directories
     * @throws dev.getelements.elements.sdk.model.exception.largeobject.LargeObjectContentNotFoundException if no
     *         content has been uploaded for the given large object id
     */
    List<ElementPathRecordMetadata> inspectElementLargeObject(String largeObjectId);

}
