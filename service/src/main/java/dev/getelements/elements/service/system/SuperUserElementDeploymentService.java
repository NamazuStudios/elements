package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.largeobject.AccessPermissions;
import dev.getelements.elements.sdk.model.largeobject.LargeObject;
import dev.getelements.elements.sdk.model.largeobject.LargeObjectReference;
import dev.getelements.elements.sdk.model.largeobject.Subjects;
import dev.getelements.elements.sdk.model.system.CreateElementDeploymentRequest;
import dev.getelements.elements.sdk.model.system.ElementDeployment;
import dev.getelements.elements.sdk.model.system.UpdateElementDeploymentRequest;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.system.ElementDeploymentService;
import jakarta.inject.Inject;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.ElementPathLoader.ELM_EXTENSION;
import static dev.getelements.elements.sdk.ElementPathLoader.ELM_MIME_TYPE;
import static dev.getelements.elements.sdk.model.system.ElementDeploymentState.*;

public class SuperUserElementDeploymentService implements ElementDeploymentService {

    private MapperRegistry registry;

    private ElementDeploymentDao elementDeploymentDao;

    private Provider<Transaction> transactionProvider;

    @Override
    public ElementDeployment createElementDeployment(final CreateElementDeploymentRequest request) {
        return getTransactionProvider().get().performAndClose(txn -> {

            final var applicationDao = txn.getDao(ApplicationDao.class);
            final var largeObjectDao = txn.getDao(LargeObjectDao.class);
            final var elementDeploymentDao = txn.getDao(ElementDeploymentDao.class);

            final var application = request.applicationNameOrId() == null
                    ? null
                    : applicationDao.getActiveApplication(request.applicationNameOrId());

            final var accessPermissions = new AccessPermissions();
            accessPermissions.setRead(Subjects.nobody());
            accessPermissions.setWrite(Subjects.nobody());
            accessPermissions.setDelete(Subjects.nobody());

            final var path = application == null
                    ? "/system/element/global/deployment.%s".formatted(ELM_EXTENSION)
                    : "/system/element/application/%s/deployment.%s".formatted(application.getName(), ELM_EXTENSION);

            final var largeObject = new LargeObject();
            largeObject.setPath(path);
            largeObject.setMimeType(ELM_MIME_TYPE);
            largeObject.setAccessPermissions(accessPermissions);

            final var largeObjectReference = getRegistry().map(
                    largeObjectDao.createLargeObject(largeObject),
                    LargeObjectReference.class
            );

            final var isReady =
                    request.elmArtifact() != null ||
                    request.elementArtifacts() != null && !request.elementArtifacts().isEmpty();

            final var state =
                    isReady && request.state() == null ? ENABLED           :
                    isReady && ENABLED.equals(request.state()) ? ENABLED   :
                    isReady && DISABLED.equals(request.state()) ? DISABLED :
                    UNLOADED;

            final var elementDeployment = new ElementDeployment(
                    null,
                    application,
                    request.apiArtifacts(),
                    request.spiArtifacts(),
                    request.elementArtifacts(),
                    largeObjectReference,
                    request.elmArtifact(),
                    request.useDefaultRepositories(),
                    request.repositories(),
                    state
            );

            return elementDeploymentDao.createElementDeployment(elementDeployment);

        });
    }

    @Override
    public Pagination<ElementDeployment> getElementDeployments(
            final int offset,
            final int count,
            final String search) {
            return getElementDeploymentDao().getElementDeployments(offset, count, search);
    }

    @Override
    public ElementDeployment getElementDeployment(final String deploymentId) {
        return getElementDeploymentDao().getElementDeployment(deploymentId);
    }

    @Override
    public ElementDeployment updateElementDeployment(
            final String deploymentId,
            final UpdateElementDeploymentRequest request) {

        return getTransactionProvider().get().performAndClose(txn -> {

            final var elementDeploymentDao = txn.getDao(ElementDeploymentDao.class);

            final var existing = elementDeploymentDao.getElementDeployment(deploymentId);
            final var largeObjectReference = refreshElmReferenceIfNecessary(txn, existing);

            final var isReady =
                    request.elmArtifact() != null ||
                    request.elementArtifacts() != null && !request.elementArtifacts().isEmpty();

            final var state =
                    isReady && request.state() == null ? ENABLED           :
                    isReady && ENABLED.equals(request.state()) ? ENABLED   :
                    isReady && DISABLED.equals(request.state()) ? DISABLED :
                    UNLOADED;

            final var elementDeployment = new ElementDeployment(
                    existing.id(),
                    existing.application(),
                    request.apiArtifacts(),
                    request.spiArtifacts(),
                    request.elementArtifacts(),
                    largeObjectReference,
                    request.elmArtifact(),
                    request.useDefaultRepositories(),
                    request.repositories(),
                    state
            );

            return elementDeploymentDao.updateElementDeployment(elementDeployment);
             
        });

    }

    private LargeObjectReference refreshElmReferenceIfNecessary(final Transaction transaction,
                                                                final ElementDeployment existing) {

        final var reference = existing.elm();

        if (reference == null) {

            final var largeObjectDao = transaction.getDao(LargeObjectDao.class);

            final var accessPermissions = new AccessPermissions();
            accessPermissions.setRead(Subjects.nobody());
            accessPermissions.setWrite(Subjects.nobody());
            accessPermissions.setDelete(Subjects.nobody());

            final var path = existing.application() == null
                    ? "/system/element/global/deployment.%s".formatted(ELM_EXTENSION)
                    : "/system/element/application/%s/deployment.%s".formatted(
                    existing.application().getName(),
                    ELM_EXTENSION
            );

            final var largeObject = new LargeObject();
            largeObject.setPath(path);
            largeObject.setMimeType(ELM_MIME_TYPE);
            largeObject.setAccessPermissions(accessPermissions);

            return getRegistry().map(
                    largeObjectDao.createLargeObject(largeObject),
                    LargeObjectReference.class
            );

        } else {
            return reference;
        }
    }

    @Override
    public void deleteDeployment(final String deploymentId) {
        getElementDeploymentDao().deleteDeployment(deploymentId);
    }

    public MapperRegistry getRegistry() {
        return registry;
    }

    @Inject
    public void setRegistry(MapperRegistry registry) {
        this.registry = registry;
    }

    public Provider<Transaction> getTransactionProvider() {
        return transactionProvider;
    }

    @Inject
    public void setTransactionProvider(Provider<Transaction> transactionProvider) {
        this.transactionProvider = transactionProvider;
    }

    public ElementDeploymentDao getElementDeploymentDao() {
        return elementDeploymentDao;
    }

    @Inject
    public void setElementDeploymentDao(ElementDeploymentDao elementDeploymentDao) {
        this.elementDeploymentDao = elementDeploymentDao;
    }

}
