package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;
import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;
import static dev.getelements.elements.sdk.cluster.id.ResourceId.randomResourceId;

public class TaskIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final DeploymentId deploymentId = randomDeploymentId();
        final ResourceId resourceId = randomResourceId(instanceId, deploymentId);
        final TaskId taskId = new TaskId(resourceId);
        Assert.assertNotNull(taskId.getResourceId());
        Assert.assertNotNull(taskId.getResourceId().getInstanceId());
        Assert.assertNotNull(taskId.getResourceId().getDeploymentId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final DeploymentId deploymentId = randomDeploymentId();
        final ResourceId resourceId = randomResourceId(instanceId, deploymentId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asBytes());
        Assert.assertEquals(duplicateTaskId, taskId);
        Assert.assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final DeploymentId deploymentId = randomDeploymentId();
        final ResourceId resourceId = randomResourceId(instanceId, deploymentId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asString());
        Assert.assertEquals(duplicateTaskId, taskId);
        Assert.assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = randomInstanceId();
        final DeploymentId deploymentId = randomDeploymentId();
        final ResourceId resourceId = randomResourceId(instanceId, deploymentId);
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
