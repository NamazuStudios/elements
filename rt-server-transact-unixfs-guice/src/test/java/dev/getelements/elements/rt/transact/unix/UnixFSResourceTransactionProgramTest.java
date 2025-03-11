package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import dev.getelements.elements.rt.transact.unix.UnixFSTransactionProgramInterpreter.ExecutionHandler;
import dev.getelements.elements.sdk.util.TemporaryFiles;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.*;

import static dev.getelements.elements.sdk.cluster.id.NodeId.randomNodeId;
import static dev.getelements.elements.rt.transact.unix.UnixFSTransactionCommandInstruction.*;
import static java.lang.String.format;
import static java.nio.ByteBuffer.allocate;
import static java.util.stream.Collectors.toList;
import static org.testng.Assert.*;
import static org.testng.FileAssert.fail;

public class UnixFSResourceTransactionProgramTest {

    private static final TemporaryFiles temporaryFiles = new TemporaryFiles(UnixFSResourceTransactionProgramTest.class);

    @DataProvider
    public static Object[][] allTestPermutations() {

        final var result = new ArrayList<Object[]>();
        final var max = 1 << UnixFSTransactionProgramExecutionPhase.values().length;

        for (int phases = 1; phases < max; ++phases) {
            for (UnixFSChecksumAlgorithm algorithm : UnixFSChecksumAlgorithm.values()) {
                result.add(new Object[]{
                        algorithm,
                        UnixFSTransactionProgramExecutionPhase.enabledPhasesFor(phases).collect(toList())}
                );
            }
        }

        return result.toArray(Object[][]::new);


    }

    @Test(dataProvider = "allTestPermutations")
    public void test(
            final UnixFSChecksumAlgorithm checksumAlgorithm,
            final List<UnixFSTransactionProgramExecutionPhase> executionPhases) {


        final String testTransactionName = format("%016X", new Random().nextLong());

        final var builder = new UnixFSTransactionProgramBuilder()
                .withByteBuffer(allocate(1024 * 32))
                .withTransactionId("test")
                .withTransactionId(testTransactionName)
                .withChecksumAlgorithm(checksumAlgorithm)
                .withNodeId(randomNodeId());

        final var phaseMap = new EnumMap<
                UnixFSTransactionProgramExecutionPhase,
                TestExecutionHandler>(UnixFSTransactionProgramExecutionPhase.class);

        for (var phase : executionPhases) {
            final var handler = new TestExecutionHandler(builder, phase);
            phaseMap.put(phase, handler);
        }

        final var program = builder
                .compile(executionPhases.toArray(UnixFSTransactionProgramExecutionPhase[]::new))
                .commit();

        for (var entry : phaseMap.entrySet()) {
            program.interpreter().execute(entry.getKey(), entry.getValue());
            entry.getValue().assertEmpty();
        }


    }

    private static class TestExecutionHandler implements ExecutionHandler {

        private final ProgramAssertion noop = (c, o, o1) -> fail("Should never be called.");

        private final Deque<ProgramAssertion> assertions = new LinkedList<>();

        public TestExecutionHandler(
                final UnixFSTransactionProgramBuilder builder,
                final UnixFSTransactionProgramExecutionPhase phase) {

            final var random = new Random();
            final var instructions = UnixFSTransactionCommandInstruction.values();

            random.ints(25, 0, instructions.length)
                    .mapToObj(ordinal -> instructions[ordinal])
                    .map(instruction -> assertionFor(builder, phase, instruction))
                    .filter(o -> o != noop)
                    .forEach(assertions::add);

        }

        private ProgramAssertion assertionFor(
                final UnixFSTransactionProgramBuilder builder,
                final UnixFSTransactionProgramExecutionPhase phase,
                final UnixFSTransactionCommandInstruction instruction) {

            var rtPath = new Path("/test/rt/path/*").appendUUIDIfWildcard();
            var resourceId = ResourceId.randomResourceId();

            switch (instruction) {
                case NOOP:
                    return noop;
                case APPLY_CONTENTS_CHANGE_FOR_RESOURCE:
                    builder.applyChangeToResourceContents(phase, resourceId);
                    return (c, rid, transactionId) -> {
                        assertEquals(c.getInstruction(), APPLY_CONTENTS_CHANGE_FOR_RESOURCE);
                        assertEquals(rid, resourceId);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case APPLY_REVERSE_PATH_CHANGE_FOR_RESOURCE:
                    builder.applyReversePathChangeToResource(phase, resourceId);
                    return (c, rid, transactionId) -> {
                        assertEquals(c.getInstruction(), APPLY_REVERSE_PATH_CHANGE_FOR_RESOURCE);
                        assertEquals(rid, resourceId);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case APPLY_PATH_CHANGE_FOR_RESOURCE:
                    builder.applyReversePathChangeToResource(phase, rtPath);
                    return (c, p, transactionId) -> {
                        assertEquals(c.getInstruction(), APPLY_PATH_CHANGE_FOR_RESOURCE);
                        assertEquals(p, rtPath);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case APPLY_TASK_CHANGES_FOR_RESOURCE_ID:
                    builder.applyChangeToTasks(phase, resourceId);
                    return (c, rid, transactionId) -> {
                        assertEquals(c.getInstruction(), APPLY_TASK_CHANGES_FOR_RESOURCE_ID);
                        assertEquals(rid, resourceId);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case CLEANUP_RESOURCE_FOR_RESOURCE_ID:
                    builder.cleanupResource(phase, resourceId);
                    return (c, rid, transactionId) -> {
                        assertEquals(c.getInstruction(), CLEANUP_RESOURCE_FOR_RESOURCE_ID);
                        assertEquals(rid, resourceId);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case CLEANUP_RESOURCE_AT_PATH:
                    builder.cleanupResource(phase, rtPath);
                    return (c, p, transactionId) -> {
                        assertEquals(c.getInstruction(), CLEANUP_RESOURCE_AT_PATH);
                        assertEquals(p, rtPath);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                case CLEANUP_TASKS_FOR_RESOURCE_ID:
                    builder.cleanupTasksForResource(phase, resourceId);
                    return (c, rid, transactionId) -> {
                        assertEquals(c.getInstruction(), CLEANUP_TASKS_FOR_RESOURCE_ID);
                        assertEquals(rid, resourceId);
                        assertEquals(transactionId, builder.getTransactionId());
                    };
                default:
                    fail("Unexpected instruction " + instruction);
                    throw new RuntimeException();
            }
        }

        private ProgramAssertion next() {
            try {
                return assertions.removeFirst();
            } catch (NoSuchElementException ex) {
                fail("Got extra instruction.", ex);
                throw ex;
            }
        }

        public void assertEmpty() {
            assertTrue(assertions.isEmpty(), "Expected more instructions.");
        }

        @Override
        public void applyContentsChange(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, resourceId, transactionId);
        }

        @Override
        public void applyReversePathsChange(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, resourceId, transactionId);
        }

        @Override
        public void applyPathChange(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final Path rtPath,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, rtPath, transactionId);
        }

        @Override
        public void applyTaskChanges(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, resourceId, transactionId);
        }

        @Override
        public void cleanupResourceId(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, resourceId, transactionId);
        }

        @Override
        public void cleanupPath(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final Path rtPath,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, rtPath, transactionId);
        }

        @Override
        public void cleanupTasks(
                final UnixFSTransactionProgram program,
                final UnixFSTransactionCommand command,
                final ResourceId resourceId,
                final String transactionId) {
            getLogger().debug("Processing {}", command);
            next().process(command, resourceId, transactionId);
        }

    }

    @FunctionalInterface
    private interface ProgramAssertion {

        void process(UnixFSTransactionCommand cmd, Object o1, Object o2);

    }

}
