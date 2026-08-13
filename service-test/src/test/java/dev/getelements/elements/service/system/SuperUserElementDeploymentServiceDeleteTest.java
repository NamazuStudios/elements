package dev.getelements.elements.service.system;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectBucket;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import java.io.IOException;
import java.util.function.Function;

import static com.google.inject.Guice.createInjector;
import static com.google.inject.name.Names.named;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Covers {@link SuperUserElementDeploymentService#deleteDeployment(String)}, specifically that it no longer
 * leaves the deployment's ELM orphaned in LargeObject/GridFS storage: both the bucket bytes and the DAO
 * metadata record must be removed, and a bucket that has nothing to delete (e.g. an ELM that was never
 * uploaded) must not prevent the metadata delete from proceeding.
 */
public class SuperUserElementDeploymentServiceDeleteTest {

    private static final String DEPLOYMENT_ID = "deployment-1";

    private static final String ELM_ID = "elm-object-1";

    @Inject
    private SuperUserElementDeploymentService service;

    @Inject
    private ElementDeploymentDao elementDeploymentDao;

    @Inject
    private LargeObjectDao largeObjectDao;

    @Inject
    private LargeObjectBucket largeObjectBucket;

    @Inject
    @Named(ROOT)
    private ElementRegistry elementRegistry;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void deletesLargeObjectBucketAndDaoWhenElmPresent() throws IOException {

        final var deployment = deploymentWithElm();
        when(elementDeploymentDao.getElementDeployment(DEPLOYMENT_ID)).thenReturn(deployment);

        service.deleteDeployment(DEPLOYMENT_ID);

        final var order = inOrder(largeObjectBucket, largeObjectDao, elementDeploymentDao);
        order.verify(largeObjectBucket).deleteLargeObject(ELM_ID);
        order.verify(largeObjectDao).deleteLargeObject(ELM_ID);
        order.verify(elementDeploymentDao).deleteDeployment(DEPLOYMENT_ID);

        verify(elementRegistry).publish(any(Event.class));

    }

    @Test
    public void proceedsWithMetadataDeleteWhenBucketHasNoBytesToDelete() throws IOException {

        final var deployment = deploymentWithElm();
        when(elementDeploymentDao.getElementDeployment(DEPLOYMENT_ID)).thenReturn(deployment);

        // Simulates an ELM that was created but never uploaded (still CREATED/UNINITIALIZED), which is
        // exactly what GridFSLargeObjectBucket#deleteLargeObject throws for a missing GridFS file.
        doThrow(new RuntimeException("simulated missing GridFS bytes"))
                .when(largeObjectBucket).deleteLargeObject(ELM_ID);

        service.deleteDeployment(DEPLOYMENT_ID);

        // The metadata record and the deployment itself must still be removed even though the bucket
        // had nothing to delete.
        verify(largeObjectDao).deleteLargeObject(ELM_ID);
        verify(elementDeploymentDao).deleteDeployment(DEPLOYMENT_ID);

    }

    @Test
    public void skipsLargeObjectDeletionWhenDeploymentHasNoElm() throws IOException {

        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(DEPLOYMENT_ID);
        when(deployment.elm()).thenReturn(null);
        when(elementDeploymentDao.getElementDeployment(DEPLOYMENT_ID)).thenReturn(deployment);

        service.deleteDeployment(DEPLOYMENT_ID);

        verifyNoInteractions(largeObjectBucket);
        verify(largeObjectDao, never()).deleteLargeObject(any());
        verify(elementDeploymentDao).deleteDeployment(DEPLOYMENT_ID);

    }

    private static ElementDeployment deploymentWithElm() {
        final var elm = mock(LargeObjectReference.class);
        when(elm.getId()).thenReturn(ELM_ID);
        final var deployment = mock(ElementDeployment.class);
        when(deployment.id()).thenReturn(DEPLOYMENT_ID);
        when(deployment.elm()).thenReturn(elm);
        return deployment;
    }

    public static class TestModule extends AbstractModule {

        private final Transaction mockTransaction = mock(Transaction.class);

        @Override
        protected void configure() {

            final var elementDeploymentDaoMock = mock(ElementDeploymentDao.class);
            final var largeObjectDaoMock = mock(LargeObjectDao.class);
            final var largeObjectBucketMock = mock(LargeObjectBucket.class);

            bind(ElementDeploymentDao.class).toInstance(elementDeploymentDaoMock);
            bind(LargeObjectDao.class).toInstance(largeObjectDaoMock);
            bind(LargeObjectBucket.class).toInstance(largeObjectBucketMock);
            bind(MapperRegistry.class).toInstance(mock(MapperRegistry.class));
            bind(ElementRegistry.class).annotatedWith(named(ROOT)).toInstance(mock(ElementRegistry.class));
            bind(Transaction.class).toInstance(mockTransaction);

            when(mockTransaction.getDao(ElementDeploymentDao.class)).thenReturn(elementDeploymentDaoMock);
            when(mockTransaction.getDao(LargeObjectDao.class)).thenReturn(largeObjectDaoMock);

            doAnswer(invocation -> {
                final Function<Transaction, ?> fn = invocation.getArgument(0);
                return fn.apply(mockTransaction);
            }).when(mockTransaction).performAndClose(any(Function.class));

        }

    }

}
