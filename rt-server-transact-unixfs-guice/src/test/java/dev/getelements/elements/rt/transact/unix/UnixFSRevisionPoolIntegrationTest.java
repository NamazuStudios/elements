package dev.getelements.elements.rt.transact.unix;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.transact.Revision;
import dev.getelements.elements.rt.transact.JournalTransactionalPersistenceDriver;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.stream.IntStream;

import static dev.getelements.elements.rt.id.NodeId.randomNodeId;
import static java.util.Collections.shuffle;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.assertEquals;

@Guice(modules = UnixFSRevisionPoolIntegrationTest.Module.class)
public class UnixFSRevisionPoolIntegrationTest {

    private static final int MAX_TEST_REVISIONS = 4096;

    @Inject
    private UnixFSRevisionPool revisionPool;

    @Inject
    private JournalTransactionalPersistenceDriver journalTransactionalPersistenceDriver;

    private final List<UnixFSRevision<?>> revisions = new ArrayList<>();

    @BeforeClass
    public void start() {
        journalTransactionalPersistenceDriver.start();
    }

    @AfterClass
    public void stop() {
        journalTransactionalPersistenceDriver.stop();
    }

    private UnixFSRevision<?> testNextRevision() {
        final UnixFSRevision<?> revision = revisionPool.createNextRevision();
        revisions.add(revision);
        return revision;
    }

    @Test
    public void testRevisionsProperlySort() {

        final List<UnixFSRevision<?>> original = IntStream
            .range(0, MAX_TEST_REVISIONS)
            .mapToObj(i -> testNextRevision())
            .collect(toList());

        final List<UnixFSRevision<?>> shuffled = new ArrayList<>(original);
        shuffle(shuffled);

        final SortedSet<UnixFSRevision<?>> expected = new TreeSet<>(original);
        final SortedSet<UnixFSRevision<?>> actual = new TreeSet<>(shuffled);

        assertEquals(expected, actual);

    }

    @Test
    public void testRevisionsCompareToZero() {
        final UnixFSRevision revision = testNextRevision();
        assertEquals(1, revision.compareTo(Revision.ZERO));
    }

    @Test
    public void testRevisionsCompareToInfinity() {
        final UnixFSRevision revision = testNextRevision();
        assertEquals(-1, revision.compareTo(Revision.INFINITY));
    }

    @Test
    public void testRevisionToAndFromString() {

        final List<UnixFSRevision<?>> original = IntStream
                .range(0, MAX_TEST_REVISIONS)
                .mapToObj(i -> testNextRevision())
                .collect(toList());

        final List<UnixFSRevision<?>> shuffled = original
            .stream()
            .map(r -> r.getUniqueIdentifier())
            .map(uid -> revisionPool.create(uid))
            .collect(toList());

        shuffle(shuffled);

        final SortedSet<UnixFSRevision<?>> expected = new TreeSet<>(original);
        final SortedSet<UnixFSRevision<?>> actual = new TreeSet<>(shuffled);

        assertEquals(expected, actual);

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
