package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.NodeId.forInstanceAndApplication;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.*;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ResourceIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        Assert.assertNotNull(resourceId.getNodeId());
        Assert.assertNotNull(resourceId.getNodeId().getInstanceId());
        Assert.assertNotNull(resourceId.getNodeId().getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId =randomResourceIdForNode(nodeId);
        final ResourceId duplicateResourceId = resourceIdFromBytes(resourceId.asBytes());
        Assert.assertEquals(duplicateResourceId, resourceId);
        Assert.assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final ResourceId duplicateResourceId = resourceIdFromString(resourceId.asString());
        Assert.assertEquals(duplicateResourceId, resourceId);
        Assert.assertEquals(duplicateResourceId.hashCode(), resourceId.hashCode());
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
            Assert.assertEquals(result, resourceId);
            Assert.assertEquals(result.hashCode(), resourceId.hashCode());
        }

    }

}
