package com.namazustudios.socialengine.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.transact.TransactionalPersistenceContext;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.nio.file.Path;

import static com.namazustudios.socialengine.rt.id.NodeId.randomNodeId;
import static java.nio.channels.FileChannel.MapMode.READ_WRITE;
import static java.nio.file.StandardOpenOption.READ;
import static java.nio.file.StandardOpenOption.WRITE;
import static org.testng.Assert.*;

@Guice(modules = UnixFSTransactionJournalIntegrationTest.Module.class)
public class UnixFSTransactionJournalIntegrationTest {

    @Inject
    private NodeId nodeId;

    @Inject
    private UnixFSTransactionJournal journal;

    @Inject
    private TransactionalPersistenceContext transactionalPersistenceContext;

    @BeforeClass
    public void start() {
        transactionalPersistenceContext.start();
    }

    @AfterClass
    public void stop() {
        transactionalPersistenceContext.stop();
    }

    @Test
    public void testHeaderCounter() throws IOException {

        final Path temp = Files.createTempFile("test", "bin");
        final UnixFSJournalHeader header = new UnixFSJournalHeader();

        try (final FileChannel fileChannel = FileChannel.open(temp, READ, WRITE)) {
            fileChannel.write(ByteBuffer.allocate(1024 * 1024));
            final MappedByteBuffer mapped = fileChannel.map(READ_WRITE, 0, 1024*1024);
            header.setByteBuffer(mapped, 0);
        }

        final UnixFSAtomicLong atomicLong = header.counter.createAtomicLong();
        atomicLong.set(0);
        assertTrue(atomicLong.compareAndSet(0, 42));
        assertEquals(atomicLong.get(), 42);

    }

    @Test
    public void testGetMultipleEntries() {
        try (final UnixFSJournalMutableEntry a = journal.newMutableEntry(nodeId);
             final UnixFSJournalMutableEntry b = journal.newMutableEntry(nodeId);) {
            final UnixFSTransactionProgramBuilder builderA = a.getProgramBuilder();
            final UnixFSTransactionProgramBuilder builderB = b.getProgramBuilder();
            assertNotSame(builderA.getByteBuffer(), builderB.getByteBuffer());
            assertEquals(builderA.getRevision(), builderB.getRevision());
        }
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {

            bind(NodeId.class).toInstance(randomNodeId());

            install(new UnixFSTransactionalPersistenceContextModule()
                .exposeDetailsForTesting()
                .withTestingDefaults()
            );

        }
    }

}
