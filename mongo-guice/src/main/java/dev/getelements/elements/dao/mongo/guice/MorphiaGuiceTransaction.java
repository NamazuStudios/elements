package dev.getelements.elements.dao.mongo.guice;

import com.google.inject.Injector;
import com.mongodb.MongoException;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.dao.Transaction;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import dev.morphia.transactions.MorphiaSession;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Random;
import java.util.function.Function;

import static com.mongodb.MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL;
import static com.mongodb.MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL;
import static dev.getelements.elements.sdk.dao.Transaction.RetryException.DEFAULT_RECOMMENDED_DELAY;
import static java.lang.System.currentTimeMillis;
import static java.lang.System.nanoTime;
import static java.util.concurrent.TimeUnit.MILLISECONDS;

public class MorphiaGuiceTransaction implements Transaction {

    private static final Logger logger = LoggerFactory.getLogger(MorphiaGuiceTransaction.class);

    @ElementDefaultAttribute(
            value = "32",
            description = "Defines the number of times a transaction will try before failing."
    )
    public static final String MAX_RETRIES = "dev.getelements.elements.mongo.transaction.retry.count";

    @ElementDefaultAttribute(
            value = "30000",
            description = "Defines how long the transaction will immediately retry until it gets a response from the server."
    )
    public static final String RETRY_TIMEOUT = "dev.getelements.elements.mongo.transaction.retry.timeout";

    private static final int NO_SUCH_TRANSACTION = 251;

    private static final int TRANSACTION_TOO_OLD = 225;

    private static final int TRANSACTION_COMMITTED_ABORTED = 246;

    private int maxRetries;

    private long retryTimeout;

    private long failures = 0;

    private final Random random = new Random();

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
    public <T> Function<Transaction, T> wrap(final Function<Transaction, T> original) {
        return txn -> {

            if (failures > getMaxRetries()) {
                throw new TooBusyException("Maximum number of retries reached");
            }

            try {
                return original.apply(txn);
            } catch (final MongoException ex) {
                if (ex.hasErrorLabel(TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                    throw retry();
                } else {
                    throw ex;
                }
            }

        };
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
            if (ex.hasErrorLabel(TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                // Tells the caller to retry the whole transaction because we know that the
                // transaction definitely failed. We call this "optimistic backoff and retry"
                // because we expect that the next attempt will succeed.
                final var delay = calculateNextDelay();
                throw new RetryException(delay);
            } else if (ex.hasErrorLabel(UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
                // This failure indicates we need to retry just the commit as the transaction
                // may have completed we just don't know it. So we are going to keep retrying
                // until we get a success, failure, or we just give up.
                retryCommitUntilSuccessOrFailure();
            } else {
                throw ex;
            }
        }

    }

    /**
     * Generates the {@link RetryException} complete with the correct timing based on the configuration.
     *
     * @return a new {@link RetryException}
     */
    private RetryException retry() {
        // Tells the caller to retry the whole transaction because we know that the
        // transaction definitely failed. We call this "optimistic backoff and retry"
        // because we expect that the next attempt will succeed.
        final var delay = calculateNextDelay();
        throw new RetryException(delay);
    }

    /**
     * Retries the commit in the event of the specific failure indicating that a transaction's status
     * is not known.
     */
    private void retryCommitUntilSuccessOrFailure() {

        Exception lastException = null;
        final long timeout = nanoTime() + MILLISECONDS.toNanos(getRetryTimeout());

        while (nanoTime() < timeout) {
            try {
                getMorphiaSession().commitTransaction();
                return;
            } catch (MongoException ex) {
                if (ex.hasErrorLabel(TRANSIENT_TRANSACTION_ERROR_LABEL)) {
                    throw retry();
                } else if (ex.hasErrorLabel(UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL)) {
                    lastException = ex;
                    logger.trace("Caught unknown transaction commit result. Retrying.", ex);
                } else {
                    throw ex;
                }
            }
        }

        logger.warn("Could not commit transaction after {} milliseconds.", getRetryTimeout(), lastException);
        throw new TooBusyException("Maximum number of retries reached", lastException);

    }

    private long calculateNextDelay() {
        return random.nextLong(++failures * DEFAULT_RECOMMENDED_DELAY);
    }

    @Override
    public void start() {
        getMorphiaSession().startTransaction();
    }

    @Override
    public void rollback() {
        try {
            if (getMorphiaSession().hasActiveTransaction()) {
                getMorphiaSession().abortTransaction();
            }
        } catch (MongoException e) {

            switch (e.getCode()) {
                case NO_SUCH_TRANSACTION:
                case TRANSACTION_TOO_OLD:
                case TRANSACTION_COMMITTED_ABORTED:
                    logger.debug("Rollback ignored: Transaction state already finalized on server.");
                    break;
                default:
                    throw e;
            }

        } finally {
            getBufferedEventPublisher().rollback();
        }
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

    public long getRetryTimeout() {
        return retryTimeout;
    }

    @Inject
    public void setRetryTimeout(@Named(RETRY_TIMEOUT) long retryTimeout) {
        this.retryTimeout = retryTimeout;
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
