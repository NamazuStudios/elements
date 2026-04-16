package dev.getelements.elements.service;

import com.google.inject.AbstractModule;
import dev.getelements.elements.sdk.dao.ItemLedgerDao;
import dev.getelements.elements.sdk.model.Pagination;
import dev.getelements.elements.sdk.model.exception.ForbiddenException;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEntry;
import dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType;
import dev.getelements.elements.service.inventory.AnonItemLedgerService;
import dev.getelements.elements.service.inventory.SuperuserItemLedgerService;
import jakarta.inject.Inject;
import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

import static com.google.inject.Guice.createInjector;
import static dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType.CREATED;
import static dev.getelements.elements.sdk.model.inventory.ItemLedgerEventType.QUANTITY_ADJUSTED;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.testng.Assert.assertSame;

public class SuperuserItemLedgerServiceTest {

    @Inject
    private SuperuserItemLedgerService service;

    @Inject
    private ItemLedgerDao itemLedgerDao;

    @BeforeMethod
    public void setup() {
        createInjector(new TestModule()).injectMembers(this);
    }

    @Test
    public void testGetLedgerEntriesDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        when(itemLedgerDao.getLedgerEntries("inv-1", 0, 10, null, null, null)).thenReturn(page);
        assertSame(service.getLedgerEntries("inv-1", 0, 10, null, null, null), page);
    }

    @Test
    public void testGetLedgerEntriesWithEventTypeFilterDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        when(itemLedgerDao.getLedgerEntries("inv-1", 5, 20, CREATED, null, null)).thenReturn(page);
        assertSame(service.getLedgerEntries("inv-1", 5, 20, CREATED, null, null), page);
    }

    @Test
    public void testGetLedgerEntriesWithTimestampRangeDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        final long from = 1_000_000L;
        final long to = 2_000_000L;
        when(itemLedgerDao.getLedgerEntries("inv-1", 0, 10, null, from, to)).thenReturn(page);
        assertSame(service.getLedgerEntries("inv-1", 0, 10, null, from, to), page);
    }

    @Test
    public void testGetLedgerEntriesForUserDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        when(itemLedgerDao.getLedgerEntriesForUser("user-1", 0, 10, null, null, null)).thenReturn(page);
        assertSame(service.getLedgerEntriesForUser("user-1", 0, 10, null, null, null), page);
    }

    @Test
    public void testGetLedgerEntriesForUserWithEventTypeFilterDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        when(itemLedgerDao.getLedgerEntriesForUser("user-1", 2, 5, QUANTITY_ADJUSTED, null, null)).thenReturn(page);
        assertSame(service.getLedgerEntriesForUser("user-1", 2, 5, QUANTITY_ADJUSTED, null, null), page);
    }

    @Test
    public void testGetLedgerEntriesForUserWithTimestampRangeDelegatesToDao() {
        @SuppressWarnings("unchecked")
        final var page = (Pagination<ItemLedgerEntry>) mock(Pagination.class);
        final long from = 1_000_000L;
        final long to = 2_000_000L;
        when(itemLedgerDao.getLedgerEntriesForUser("user-1", 0, 10, null, from, to)).thenReturn(page);
        assertSame(service.getLedgerEntriesForUser("user-1", 0, 10, null, from, to), page);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testAnonServiceGetLedgerEntriesThrowsForbidden() {
        new AnonItemLedgerService().getLedgerEntries("inv-1", 0, 10, null, null, null);
    }

    @Test(expectedExceptions = ForbiddenException.class)
    public void testAnonServiceGetLedgerEntriesForUserThrowsForbidden() {
        new AnonItemLedgerService().getLedgerEntriesForUser("user-1", 0, 10, null, null, null);
    }

    private static class TestModule extends AbstractModule {
        @Override
        protected void configure() {
            bind(ItemLedgerDao.class).toInstance(mock(ItemLedgerDao.class));
        }
    }
}
