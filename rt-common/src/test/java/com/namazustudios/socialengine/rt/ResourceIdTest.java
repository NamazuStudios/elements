package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.InstanceId;
import com.namazustudios.socialengine.rt.id.NodeId;
import com.namazustudios.socialengine.rt.id.ResourceId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ResourceIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = new InstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomUUID());
        final ResourceId resourceId = new ResourceId(nodeId);
        assertNotNull(resourceId.getNodeId());
        assertNotNull(resourceId.getNodeId().getInstanceId());
        assertNotNull(resourceId.getNodeId().getApplicationUuid());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = new InstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomUUID());
        final ResourceId resourceId = new ResourceId(nodeId);
        final ResourceId duplicateResourceId = new ResourceId(resourceId.asBytes());
        assertEquals(duplicateResourceId, resourceId);
        assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = new InstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomUUID());
        final ResourceId resourceId = new ResourceId(nodeId);
        final ResourceId duplicateResourceId = new ResourceId(resourceId.asString());
        assertEquals(duplicateResourceId, resourceId);
        assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = new InstanceId();
        final NodeId nodeId = new NodeId(instanceId, randomUUID());
        final ResourceId resourceId = new ResourceId(nodeId);

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(resourceId);
            }

            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            assertEquals(result, resourceId);
        }

    }

}
