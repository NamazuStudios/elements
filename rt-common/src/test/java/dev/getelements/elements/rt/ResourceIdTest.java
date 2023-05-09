package dev.getelements.elements.rt;

import dev.getelements.elements.rt.id.InstanceId;
import dev.getelements.elements.rt.id.NodeId;
import dev.getelements.elements.rt.id.ResourceId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.rt.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.rt.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.rt.id.NodeId.forInstanceAndApplication;
import static dev.getelements.elements.rt.id.ResourceId.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ResourceIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        assertNotNull(resourceId.getNodeId());
        assertNotNull(resourceId.getNodeId().getInstanceId());
        assertNotNull(resourceId.getNodeId().getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId =randomResourceIdForNode(nodeId);
        final ResourceId duplicateResourceId = resourceIdFromBytes(resourceId.asBytes());
        assertEquals(duplicateResourceId, resourceId);
        assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final ResourceId duplicateResourceId = resourceIdFromString(resourceId.asString());
        assertEquals(duplicateResourceId, resourceId);
        assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);

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
            assertEquals(result.hashCode(), resourceId.hashCode());
        }

    }

}
