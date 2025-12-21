package dev.getelements.elements.dao.mongo.guice;

import dev.getelements.elements.sdk.ElementRegistry;
import dev.getelements.elements.sdk.Event;
import dev.getelements.elements.sdk.dao.Transaction;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

import static dev.getelements.elements.sdk.ElementRegistry.ROOT;

/**
 * Buffers events during a transaction and publishes them only if the transaction commits. In the event of a rollback,
 * this clears all events being buffered as they are no longer valid.
 */
public class MongoTransactionBufferedEventPublisher implements Consumer<Event> {

    private final List<Event> buffer = new ArrayList<>();

    private Transaction transaction;

    private ElementRegistry elementRegistry;

    @Override
    public void accept(final Event event) {

        final var transactionalEvent = new Event.Builder()
                .from(event)
                .argument(getTransaction())
                .build();

        getElementRegistry().publish(transactionalEvent);
        buffer.add(event);

    }

    public void postCommit() {
        buffer.forEach(getElementRegistry()::publish);
        buffer.clear();
    }

    public void rollback() {
        buffer.clear();
    }

    public Transaction getTransaction() {
        return transaction;
    }

    @Inject
    public void setTransaction(Transaction transaction) {
        this.transaction = transaction;
    }

    public ElementRegistry getElementRegistry() {
        return elementRegistry;
    }

    @Inject
    public void setElementRegistry(@Named(ROOT) ElementRegistry elementRegistry) {
        this.elementRegistry = elementRegistry;
    }

}
