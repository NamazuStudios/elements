package dev.getelements.elements.service;

import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.ElementDeploymentState;
import dev.getelements.elements.service.largeobject.DeploymentElmReloader;
import org.mockito.Mockito;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.testng.Assert.expectThrows;

/**
 * Unit coverage for #71: closing a LargeObject content-upload stream must bump the version of every
 * enabled {@link ElementDeployment} whose ELM references that object, which publishes
 * {@code ELEMENT_DEPLOYMENT_UPDATED} and makes the runtime service reconcile and reload the deployment
 * with the fresh content. A failed upload must not trigger a reload, and a failure while triggering
 * the reload must not fail the (already committed) upload.
 */
public class DeploymentElmReloaderTest {

    private ElementDeploymentDao elementDeploymentDao;

    private DeploymentElmReloader reloader;

    @BeforeMethod
    public void setup() {
        elementDeploymentDao = mock(ElementDeploymentDao.class);
        reloader = new DeploymentElmReloader();
        reloader.setElementDeploymentDao(elementDeploymentDao);
    }

    @Test
    public void closingStreamBumpsVersionOfDeploymentsReferencingElm() throws Exception {

        final var elmDeployment = deployment("dep-1", "lo-1");
        final var otherElmDeployment = deployment("dep-2", "lo-OTHER");
        final var noElmDeployment = deployment("dep-3", null);

        Mockito.when(elementDeploymentDao.getElementDeploymentsByState(ElementDeploymentState.ENABLED))
                .thenReturn(List.of(elmDeployment, otherElmDeployment, noElmDeployment));

        final var output = reloader.reloadOnClose(OutputStream.nullOutputStream(), "lo-1");
        output.write(new byte[] {1, 2, 3});
        output.flush();
        output.close();

        verify(elementDeploymentDao).updateElementDeployment(elmDeployment);
        verify(elementDeploymentDao, never()).updateElementDeployment(otherElmDeployment);
        verify(elementDeploymentDao, never()).updateElementDeployment(noElmDeployment);

    }

    @Test
    public void failureToTriggerReloadDoesNotFailTheUpload() throws Exception {

        Mockito.when(elementDeploymentDao.getElementDeploymentsByState(ElementDeploymentState.ENABLED))
                .thenThrow(new RuntimeException("database unavailable"));

        final var output = reloader.reloadOnClose(OutputStream.nullOutputStream(), "lo-1");

        output.close(); // must not throw: the content itself was committed successfully
        verify(elementDeploymentDao).getElementDeploymentsByState(ElementDeploymentState.ENABLED);

    }

    @Test
    public void failedUploadDoesNotTriggerReload() throws Exception {

        final var failingStream = new OutputStream() {
            @Override
            public void write(final int b) {}
            @Override
            public void close() throws IOException {
                throw new IOException("simulated storage failure");
            }
        };

        final var output = reloader.reloadOnClose(failingStream, "lo-1");

        expectThrows(IOException.class, output::close);
        verify(elementDeploymentDao, never()).getElementDeploymentsByState(any());

    }

    private ElementDeployment deployment(final String id, final String elmLargeObjectId) {

        final var deployment = mock(ElementDeployment.class);
        Mockito.when(deployment.id()).thenReturn(id);

        if (elmLargeObjectId != null) {
            final var elm = new LargeObjectReference();
            elm.setId(elmLargeObjectId);
            Mockito.when(deployment.elm()).thenReturn(elm);
        }

        return deployment;

    }

}
