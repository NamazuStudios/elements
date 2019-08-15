package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class NodeIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomApplicationId());
        assertNotNull(nodeId.getInstanceId());
        assertNotNull(nodeId.getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = new NodeId(nodeId.asBytes());
        assertEquals(duplicateNodeId, nodeId);
        assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomApplicationId());
        final NodeId duplicateNodeId = new NodeId(nodeId.asString());
        assertEquals(duplicateNodeId, nodeId);
        assertEquals(duplicateNodeId.hashCode(), nodeId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomApplicationId());

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

}
