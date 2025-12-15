package dev.getelements.elements.service.receipt;

import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.TypeLiteral;
import com.mongodb.MongoException;
import dev.getelements.elements.dao.mongo.guice.MongoTransactionBufferedEventPublisher;
import dev.getelements.elements.sdk.annotation.ElementDefaultAttribute;
import dev.getelements.elements.sdk.dao.*;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.InternalException;
import dev.getelements.elements.sdk.model.exception.NotFoundException;
import dev.getelements.elements.sdk.model.exception.TooBusyException;
import dev.getelements.elements.sdk.model.profile.Profile;
import dev.getelements.elements.sdk.model.receipt.Receipt;
import dev.getelements.elements.sdk.model.user.User;
import dev.getelements.elements.sdk.model.util.MapperRegistry;
import dev.getelements.elements.sdk.service.receipt.ReceiptService;
import dev.getelements.elements.sdk.util.LazyValue;
import dev.getelements.elements.sdk.util.SimpleLazyValue;
import dev.getelements.elements.service.util.ServicesMapperRegistryProvider;
import dev.morphia.transactions.MorphiaSession;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Provider;
import org.bson.types.ObjectId;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.Optional;
import java.util.Random;
import java.util.function.Supplier;

import static com.google.inject.name.Names.named;
import static com.mongodb.MongoException.TRANSIENT_TRANSACTION_ERROR_LABEL;
import static com.mongodb.MongoException.UNKNOWN_TRANSACTION_COMMIT_RESULT_LABEL;
import static dev.getelements.elements.sdk.dao.Transaction.RetryException.DEFAULT_RECOMMENDED_DELAY;
import static dev.getelements.elements.sdk.model.Constants.API_OUTSIDE_URL;
import static java.lang.Thread.sleep;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public abstract class AbstractReceiptServiceTest {

    protected static final int INVOCATION_COUNT = 10;

    @Inject
    protected ReceiptDao receiptDao;

    @Inject
    private Provider<Transaction> transactionProvider;

    protected static final ArrayList<Receipt> createdReceipts = new ArrayList<>();

    protected void setup() {

        when(receiptDao.createReceipt(any())).then(mockInvocation -> {
            final Receipt receipt = mockInvocation.getArgument(0);

            try {
                final var existing = receiptDao.getReceipt(receipt.getSchema(), receipt.getOriginalTransactionId());

                if(existing != null) {
                    return existing;
                }
            } catch (NotFoundException ignored) {}

            receipt.setId(String.valueOf(new ObjectId()));
            createdReceipts.add(receipt);
            return receipt;
        });


        when(receiptDao.getReceipt(anyString()))
                .then( mockInvocation ->
                        createdReceipts.stream()
                                .filter(r -> r.getId().equals(mockInvocation.getArgument(0)))
                                .findFirst()
                                .get());

        when(receiptDao.getReceipt(anyString(), anyString()))
                .then( mockInvocation -> {
                    final var scheme = mockInvocation.getArgument(0);
                    final var transactionId = mockInvocation.getArgument(1);

                    return createdReceipts.stream()
                            .filter(r -> r.getSchema().equals(scheme))
                            .filter(t -> t.getOriginalTransactionId().equals(transactionId))
                            .findFirst()
                            .orElseThrow(() -> new NotFoundException("Not found"));
                });


        when(receiptDao.getReceipts(any(User.class), anyInt(), anyInt(), anyString()))
                .then( mockInvocation -> Pagination.from(createdReceipts.stream()));


        doAnswer(mockInvocation -> {
            final var receiptId = mockInvocation.getArgument(0);
            final var matchingReceipt = createdReceipts.stream()
                    .filter(r -> r.getId().equals(receiptId)).findFirst()
                    .orElseThrow(() -> new NotFoundException("Not found"));
            createdReceipts.remove(matchingReceipt);
            return receiptId;
        }).when(receiptDao).deleteReceipt(anyString());

//        doAnswer(mockInvocation -> new TestTransaction()).when(transactionProvider).get();

    }

    public abstract static class AbstractTestModule extends AbstractModule {

        @Override
        protected void configure() {

            bind(ApplicationConfigurationDao.class).toInstance(mock(ApplicationConfigurationDao.class));
            bind(SessionDao.class).toInstance(mock(SessionDao.class));
            bind(ItemDao.class).toInstance(mock(ItemDao.class));
            bind(InventoryItemDao.class).toInstance(mock(InventoryItemDao.class));
            bind(ReceiptDao.class).toInstance(mock(ReceiptDao.class));
            bind(RewardIssuanceDao.class).toInstance(mock(RewardIssuanceDao.class));
            bind(ProfileDao.class).toInstance(mock(ProfileDao.class));
            bind(UserDao.class).toInstance(mock(UserDao.class));

            bind(new TypeLiteral<Optional<Profile>>(){}).toInstance(Optional.empty());
            bind(new TypeLiteral<Supplier<Profile>>(){}).toInstance(mock(Supplier.class));

            final var userSpy = spy(User.class);
            userSpy.setId("permittedUserId");
            bind(User.class).toInstance(userSpy);

            // Service Level Dependencies
            bind(MapperRegistry.class).toProvider(ServicesMapperRegistryProvider.class);
            bind(Transaction.class).toProvider(TestTransactionProvider.class);

            bind(String.class).annotatedWith(named(API_OUTSIDE_URL)).toInstance("http://localhost:8080/api/rest");
        }

    }

    public static class TestTransactionProvider implements Provider<Transaction> {

        @Inject
        private Injector injector;

        @Override
        public Transaction get() {
            return new TestTransaction(injector);
        }
    }

    public static class TestTransaction implements Transaction {

        private Injector injector;

        public TestTransaction(Injector injector) {
            this.injector = injector;
        }

        @Override
        public <DaoT> DaoT getDao(final Class<DaoT> daoT) {
            return injector.getInstance(daoT);
        }

        @Override
        public boolean isActive() {
            return true;
        }

        @Override
        public void commit() {}

        private void handle(final MongoException ex, final Runnable unknown) {}

        @Override
        public void start() {}

        @Override
        public void rollback() {}

        @Override
        public void close() {}

    }
}
