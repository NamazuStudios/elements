package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import static dev.getelements.elements.rt.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.rt.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.rt.id.NodeId.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class NodeIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        assertNotNull(nodeId.getInstanceId());
        assertNotNull(nodeId.getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = nodeIdFromBytes(nodeId.asBytes());
        assertEquals(duplicateNodeId, nodeId);
        assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = nodeIdFromString(nodeId.asString());
        assertEquals(duplicateNodeId, nodeId);
        assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
    }

    @Test
    public void testSerializationByteArray() throws Exception {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(nodeId);
            }

            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            assertEquals(result, nodeId);
            assertEquals(result.hashCode(), nodeId.hashCode());
        }

    }

    @Test
    public void testToByteBuffer() {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ByteBuffer byteBuffer = ByteBuffer.allocate(NodeId.getSizeInBytes());

        nodeId.toByteBuffer(byteBuffer);
        assertEquals(byteBuffer.position(), NodeId.getSizeInBytes());

        byteBuffer.flip();

        final NodeId read = nodeIdFromByteBuffer(byteBuffer);
        assertEquals(read, nodeId);
        assertEquals(byteBuffer.position(), NodeId.getSizeInBytes());

    }

    @Test
    public void testSerializationByteBufferInPlace() {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ByteBuffer byteBuffer = ByteBuffer.allocate(NodeId.getSizeInBytes());

        nodeId.toByteBuffer(byteBuffer, 0);
        assertEquals(byteBuffer.position(), 0);
        assertEquals(byteBuffer.limit(), byteBuffer.capacity());

        final NodeId read = nodeIdFromByteBuffer(byteBuffer, 0);
        assertEquals(read, nodeId);
        assertEquals(byteBuffer.position(), 0);
        assertEquals(byteBuffer.limit(), byteBuffer.capacity());

    }


}
