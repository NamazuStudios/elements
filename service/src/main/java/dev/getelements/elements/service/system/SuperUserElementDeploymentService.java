package dev.getelements.elements.service.system;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.ApplicationDao;
import dev.getelements.elements.sdk.dao.ElementDeploymentDao;
import dev.getelements.elements.sdk.dao.LargeObjectDao;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.application.Application;
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
import jakarta.inject.Named;
import jakarta.inject.Provider;

import static dev.getelements.elements.sdk.ElementPathLoader.ELM_EXTENSION;
import static dev.getelements.elements.sdk.ElementPathLoader.ELM_MIME_TYPE;
import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

public class SuperUserElementDeploymentService implements ElementDeploymentService {

    private MapperRegistry registry;

    private ElementDeploymentDao elementDeploymentDao;

    private Provider<Transaction> transactionProvider;

    private ElementRegistry elementRegistry;

    @Override
    public ElementDeployment createElementDeployment(final CreateElementDeploymentRequest request) {
        final var created = getTransactionProvider().get().performAndClose(txn -> {

            final var applicationDao = txn.getDao(ApplicationDao.class);
            final var largeObjectDao = txn.getDao(LargeObjectDao.class);
            final var elementDeploymentDao = txn.getDao(ElementDeploymentDao.class);

            final var application = request.applicationNameOrId() == null
                    ? null
                    : applicationDao.getActiveApplication(request.applicationNameOrId());

            final var largeObject = createLargeObjectFor(application);

            final var largeObjectReference = getRegistry().map(
                    largeObjectDao.createLargeObject(largeObject),
                    LargeObjectReference.class
            );

            final var elementDeployment = new ElementDeployment(
                    null,
                    application,
                    largeObjectReference,
                    request.pathSpiBuiltins(),
                    request.pathSpiClassPaths(),
                    request.pathAttributes(),
                    request.elements(),
                    request.packages(),
                    request.useDefaultRepositories(),
                    request.repositories(),
                    request.effectiveState(),
                    0L
            );

            return elementDeploymentDao.createElementDeployment(elementDeployment);

        });

        // Publish service-level event after transaction commits
        getElementRegistry().publish(Event.builder()
                .argument(created)
                .named(ELEMENT_DEPLOYMENT_CREATED)
                .build());

        return created;
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

        final var updated = getTransactionProvider().get().performAndClose(txn -> {

            final var elementDeploymentDao = txn.getDao(ElementDeploymentDao.class);

            final var existing = elementDeploymentDao.getElementDeployment(deploymentId);
            final var largeObjectReference = refreshElmReferenceIfNecessary(txn, existing);

            final var elementDeployment = new ElementDeployment(
                    existing.id(),
                    existing.application(),
                    largeObjectReference,
                    request.pathSpiBuiltins(),
                    request.pathSpiClassPaths(),
                    request.pathAttributes(),
                    request.elements(),
                    request.packages(),
                    request.useDefaultRepositories(),
                    request.repositories(),
                    request.effectiveState(),
                    existing.version()
            );

            return elementDeploymentDao.updateElementDeployment(elementDeployment);

        });

        // Publish service-level event after transaction commits
        getElementRegistry().publish(Event.builder()
                .argument(updated)
                .named(ELEMENT_DEPLOYMENT_UPDATED)
                .build());

        return updated;

    }

    private LargeObjectReference refreshElmReferenceIfNecessary(final Transaction transaction,
                                                                final ElementDeployment existing) {

        final var reference = existing.elm();

        if (reference == null) {

            final var largeObjectDao = transaction.getDao(LargeObjectDao.class);

            final var largeObject = createLargeObjectFor(existing.application());

            return getRegistry().map(
                    largeObjectDao.createLargeObject(largeObject),
                    LargeObjectReference.class
            );

        } else {
            return reference;
        }
    }

    private static LargeObject createLargeObjectFor(final Application application) {

        final var path = application == null
                ? "/system/element/global/deployment.%s".formatted(ELM_EXTENSION)
                : "/system/element/application/%s/deployment.%s".formatted(
                application.getName(),
                ELM_EXTENSION
        );

        final var accessPermissions = new AccessPermissions();
        accessPermissions.setRead(Subjects.nobody());
        accessPermissions.setWrite(Subjects.nobody());
        accessPermissions.setDelete(Subjects.nobody());

        final var largeObject = new LargeObject();
        largeObject.setPath(path);
        largeObject.setMimeType(ELM_MIME_TYPE);
        largeObject.setAccessPermissions(accessPermissions);
        return largeObject;

    }

    @Override
    public void deleteDeployment(final String deploymentId) {
        // Fetch the deployment before deleting so we can emit it in the event
        final var deployment = getElementDeploymentDao().getElementDeployment(deploymentId);

        getElementDeploymentDao().deleteDeployment(deploymentId);

        // Publish service-level event after deletion
        getElementRegistry().publish(Event.builder()
                .argument(deployment)
                .named(ELEMENT_DEPLOYMENT_DELETED)
                .build());
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

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(@Named(ROOT) ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
