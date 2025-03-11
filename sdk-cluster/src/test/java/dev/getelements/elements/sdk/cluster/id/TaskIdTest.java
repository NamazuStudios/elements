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
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceIdForNode;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TaskIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        Assert.assertNotNull(taskId.getNodeId());
        Assert.assertNotNull(taskId.getResourceId());
        Assert.assertNotNull(taskId.getNodeId().getInstanceId());
        Assert.assertNotNull(taskId.getNodeId().getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asBytes());
        Assert.assertEquals(duplicateTaskId, taskId);
        Assert.assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asString());
        Assert.assertEquals(duplicateTaskId, taskId);
        Assert.assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(taskId);
            }

            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            Assert.assertEquals(result, taskId);
            Assert.assertEquals(result.hashCode(), taskId.hashCode());
        }

    }

}
