package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.nio.ByteBuffer;

import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.NodeId.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class NodeIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        Assert.assertNotNull(nodeId.getInstanceId());
        Assert.assertNotNull(nodeId.getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = nodeIdFromBytes(nodeId.asBytes());
        Assert.assertEquals(duplicateNodeId, nodeId);
        Assert.assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = nodeIdFromString(nodeId.asString());
        Assert.assertEquals(duplicateNodeId, nodeId);
        Assert.assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
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
            Assert.assertEquals(result, nodeId);
            Assert.assertEquals(result.hashCode(), nodeId.hashCode());
        }

    }

    @Test
    public void testToByteBuffer() {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ByteBuffer byteBuffer = ByteBuffer.allocate(NodeId.getSizeInBytes());

        nodeId.toByteBuffer(byteBuffer);
        Assert.assertEquals(byteBuffer.position(), NodeId.getSizeInBytes());

        byteBuffer.flip();

        final NodeId read = nodeIdFromByteBuffer(byteBuffer);
        Assert.assertEquals(read, nodeId);
        Assert.assertEquals(byteBuffer.position(), NodeId.getSizeInBytes());

    }

    @Test
    public void testSerializationByteBufferInPlace() {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ByteBuffer byteBuffer = ByteBuffer.allocate(NodeId.getSizeInBytes());

        nodeId.toByteBuffer(byteBuffer, 0);
        Assert.assertEquals(byteBuffer.position(), 0);
        Assert.assertEquals(byteBuffer.limit(), byteBuffer.capacity());

        final NodeId read = nodeIdFromByteBuffer(byteBuffer, 0);
        Assert.assertEquals(read, nodeId);
        Assert.assertEquals(byteBuffer.position(), 0);
        Assert.assertEquals(byteBuffer.limit(), byteBuffer.capacity());

    }


}
