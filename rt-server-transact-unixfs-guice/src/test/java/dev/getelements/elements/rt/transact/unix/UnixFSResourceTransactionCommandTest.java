package dev.getelements.elements.rt.transact.unix;

import dev.getelements.elements.sdk.cluster.path.Path;
import dev.getelements.elements.sdk.cluster.id.ResourceId;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import java.util.ArrayList;

import static java.nio.ByteBuffer.allocate;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertFalse;

public class UnixFSResourceTransactionCommandTest {

    @DataProvider
    public static Object[][] allPhasesAndInstructions() {

        final var result = new ArrayList<Object[]>();

        for (var phase : UnixFSTransactionProgramExecutionPhase.values()) {
            for (var instruction : UnixFSTransactionCommandInstruction.values()) {
                result.add(new Object[]{phase, instruction});
            }
        }

        return result.toArray(Object[][]::new);

    }

    @Test(dataProvider = "allPhasesAndInstructions")
    public void testSerializeAndDeserializeIndividual(
            final UnixFSTransactionProgramExecutionPhase phase,
            final UnixFSTransactionCommandInstruction instruction) {

        final var buffer = allocate(1024*32);

        final var rtPath = new Path("test");
        final var fsPath = java.nio.file.Paths.get("test");
        final var resourceId = ResourceId.randomResourceId();

        final var serialized = UnixFSTransactionCommand.builder()
                .withPhase(phase)
                .withInstruction(instruction)
                .addStringParameter("test")
                .addRTPathParameter(rtPath)
                .addFSPathParameter(fsPath)
                .addResourceIdParameter(resourceId)
                .build(buffer);

        assertEquals(serialized.getPhase(), phase);
        assertEquals(serialized.getInstruction(), instruction);
        assertEquals(serialized.getParameterAt(0).asString(), "test");
        assertEquals(serialized.getParameterAt(1).asRTPath(), rtPath);
        assertEquals(serialized.getParameterAt(2).asFSPath(), fsPath);
        assertEquals(serialized.getParameterAt(3).asResourceId(), resourceId);

        final var deserialized = UnixFSTransactionCommand.from(buffer.flip());
        assertEquals(deserialized.getPhase(), phase);
        assertEquals(deserialized.getInstruction(), instruction);
        assertEquals(deserialized.getParameterAt(0).asString(), "test");
        assertEquals(deserialized.getParameterAt(1).asRTPath(), rtPath);
        assertEquals(deserialized.getParameterAt(2).asFSPath(), fsPath);
        assertEquals(deserialized.getParameterAt(3).asResourceId(), resourceId);

    }

    @DataProvider
    public static Object[][] allPhases() {

        final var result = new ArrayList<Object[]>();

        for (var phase : UnixFSTransactionProgramExecutionPhase.values()) {
            result.add(new Object[]{phase});
        }

        return result.toArray(Object[][]::new);

    }

    @Test(dataProvider = "allPhases")
    public void testSerializeAndDeserializeMultiple( final UnixFSTransactionProgramExecutionPhase phase) {

        final var buffer = allocate(1024*32);

        final var rtPath = new Path("test");
        final var fsPath = java.nio.file.Paths.get("test");
        final var resourceId = ResourceId.randomResourceId();

        for (var instruction : UnixFSTransactionCommandInstruction.values()) {

            final var serialized = UnixFSTransactionCommand.builder()
                    .withPhase(phase)
                    .withInstruction(instruction)
                    .addStringParameter("test")
                    .addRTPathParameter(rtPath)
                    .addFSPathParameter(fsPath)
                    .addResourceIdParameter(resourceId)
                    .build(buffer);

            assertEquals(serialized.getPhase(), phase);
            assertEquals(serialized.getInstruction(), instruction);
            assertEquals(serialized.getParameterAt(0).asString(), "test");
            assertEquals(serialized.getParameterAt(1).asRTPath(), rtPath);
            assertEquals(serialized.getParameterAt(2).asFSPath(), fsPath);
            assertEquals(serialized.getParameterAt(3).asResourceId(), resourceId);

        }

        buffer.flip();

        for (var instruction : UnixFSTransactionCommandInstruction.values()) {
            final var deserialized = UnixFSTransactionCommand.from(buffer);
            assertEquals(deserialized.getPhase(), phase);
            assertEquals(deserialized.getInstruction(), instruction);
            assertEquals(deserialized.getParameterAt(0).asString(), "test");
            assertEquals(deserialized.getParameterAt(1).asRTPath(), rtPath);
            assertEquals(deserialized.getParameterAt(2).asFSPath(), fsPath);
            assertEquals(deserialized.getParameterAt(3).asResourceId(), resourceId);
        }

        assertFalse(buffer.hasRemaining());

    }

}
