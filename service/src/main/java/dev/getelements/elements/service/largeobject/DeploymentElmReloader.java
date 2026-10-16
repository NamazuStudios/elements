package dev.getelements.elements.service.largeobject;

import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import jakarta.inject.Inject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Reloads deployments whose ELM content has changed. Uploading new bytes to a LargeObject only touches
 * the object's bucket -- it never modifies the {@link ElementDeployment} document referencing it, so the
 * runtime service had no way to notice the change and the deployment kept running (and reporting) the
 * previously loaded content indefinitely (#71).
 * <p>
 * Wrapping the content-upload stream defers the reload until the upload completes: closing the stream
 * commits the new content, at which point the version of every enabled deployment whose ELM references
 * the object is bumped. The version bump publishes {@code ELEMENT_DEPLOYMENT_UPDATED}, which the runtime
 * service already consumes to reconcile and reload the deployment with the fresh content.
 */
public class DeploymentElmReloader {

    private static final Logger logger = LoggerFactory.getLogger(DeploymentElmReloader.class);

    private ElementDeploymentDao elementDeploymentDao;

    /**
     * Wraps the supplied stream so that closing it (completing the content upload) bumps the version of
     * every enabled deployment whose ELM references the supplied LargeObject. Failures to trigger the
     * reload are logged and never fail the upload itself, which has already committed by the time the
     * close reaches this wrapper.
     *
     * @param delegate the stream writing the object's content
     * @param largeObjectId the LargeObject being written
     * @return the wrapping stream
     */
    public OutputStream reloadOnClose(final OutputStream delegate, final String largeObjectId) {

        return new OutputStream() {

            @Override
            public void write(final int b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(final byte[] b) throws IOException {
                delegate.write(b);
            }

            @Override
            public void write(final byte[] b, final int off, final int len) throws IOException {
                delegate.write(b, off, len);
            }

            @Override
            public void flush() throws IOException {
                delegate.flush();
            }

            @Override
            public void close() throws IOException {
                // A failed upload must not trigger a reload: only a successful close commits new content.
                delegate.close();
                try {
                    bumpElmDeploymentVersions(largeObjectId);
                } catch (RuntimeException ex) {
                    logger.warn(
                            "Content of LargeObject {} was updated, but reloading the deployments using it failed",
                            largeObjectId,
                            ex
                    );
                }
            }

        };

    }

    private void bumpElmDeploymentVersions(final String largeObjectId) {

        final List<ElementDeployment> deployments = getElementDeploymentDao()
                .getElementDeploymentsByState(ElementDeploymentState.ENABLED);

        deployments.stream()
                .filter(d -> d.elm() != null)
                .filter(d -> largeObjectId.equals(d.elm().getId()))
                .forEach(d -> {
                    logger.info("ELM content of deployment {} changed; bumping version to force reload", d.id());
                    getElementDeploymentDao().updateElementDeployment(d);
                });

    }

    public ElementDeploymentDao getElementDeploymentDao() {
        return elementDeploymentDao;
    }

    @Inject
    public void setElementDeploymentDao(ElementDeploymentDao elementDeploymentDao) {
        this.elementDeploymentDao = elementDeploymentDao;
    }

}
