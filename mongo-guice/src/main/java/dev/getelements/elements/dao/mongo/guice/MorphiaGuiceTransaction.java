package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.Injector;
import com.mongodb.MongoException;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import dev.morphia.transactions.MorphiaSession;
import jakarta.inject.Inject;
import jakarta.inject.Named;

import java.util.Random;

import static com.mongodb.MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL;
import static com.mongodb.MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL;
import static dev.getelements.elements.sdk.dao.Transaction.RetryException.DEFAULT_RECOMMENDED_DELAY;
import static java.lang.Thread.sleep;

public class MorphiaGuiceTransaction implements Transaction {

    @ElementDefaultAttribute(
            value = "32",
            description = "Defines the number of times a transaction will try before failing."
    )
    public static final String MAX_RETRIES = "dev.getelements.elements.mongo.transaction.retry.count";

    private int maxRetries;

    private long failures = 0;

    private final LazyValue<Random> random = new SimpleLazyValue<>(Random::new);

    private Injector injector;

    private MorphiaSession morphiaSession;

    private MongoTransactionBufferedEventPublisher bufferedEventPublisher;

    @Override
    public <DaoT> DaoT getDao(final Class<DaoT> daoT) {
        return getInjector().getInstance(daoT);
    }

    @Override
    public boolean isActive() {
        return getMorphiaSession().hasActiveTransaction();
    }

    @Override
    public void commit() {

        if (failures > getMaxRetries()) {
            throw new TooBusyException("Maximum number of retries reached");
        }

        try {
            getMorphiaSession().commitTransaction();
            getBufferedEventPublisher().postCommit();
        } catch (MongoException ex) {
            handle(ex, this::retryCommitUntilSuccessOrFailure);
        }

    }

    private void retryCommitUntilSuccessOrFailure() {

        while (failures < getMaxRetries()) {
            try {
                final var delay = calculateNextDelay();
                sleep(delay);
                getMorphiaSession().commitTransaction();
                return;
            } catch (MongoException ex) {
                handle(ex, () -> {});
            } catch (InterruptedException ex) {
                throw new InternalException(ex);
            }
        }

        throw new TooBusyException("Maximum number of retries reached");

    }

    private long calculateNextDelay() {
        return random.get().nextLong(++failures * DEFAULT_RECOMMENDED_DELAY);
    }

    private void handle(final MongoException ex, final Runnable unknown) {
        if (ex.hasErrorLabel(TRANSIENT_TRANSACTION_ERROR_LABEL)) {
            // Tells the caller to retry the whole transaction because we know that the
            // transaction definitely failed. We call this "optimistic backoff and retry"
            // because we expect that the next attempt will succeed.
            final var delay = calculateNextDelay();
            throw new RetryException(delay);
        } else if (ex.hasErrorLabel(UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
            // We handle the unknown edge case.
            unknown.run();
        } else {
            throw ex;
        }
    }

    @Override
    public void start() {
        getMorphiaSession().startTransaction();
    }

    @Override
    public void rollback() {
        getMorphiaSession().abortTransaction();
        getBufferedEventPublisher().rollback();
    }

    @Override
    public void close() {
        getMorphiaSession().close();
    }

    public int getMaxRetries() {
        return maxRetries;
    }

    @Inject
    public void setMaxRetries(@Named(MAX_RETRIES) int maxRetries) {
        this.maxRetries = maxRetries;
    }

    public Injector getInjector() {
        return injector;
    }

    @Inject
    public void setInjector(final Injector injector) {
        this.injector = injector;
    }

    public MorphiaSession getMorphiaSession() {
        return morphiaSession;
    }

    @Inject
    public void setMorphiaSession(final MorphiaSession morphiaSession) {
        this.morphiaSession = morphiaSession;
    }

    public MongoTransactionBufferedEventPublisher getBufferedEventPublisher() {
        return bufferedEventPublisher;
    }

    @Inject
    public void setBufferedEventPublisher(MongoTransactionBufferedEventPublisher bufferedEventPublisher) {
        this.bufferedEventPublisher = bufferedEventPublisher;
    }

}
