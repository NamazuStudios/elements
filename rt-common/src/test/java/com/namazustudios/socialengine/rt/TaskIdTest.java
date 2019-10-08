package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.*;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static com.namazustudios.socialengine.rt.id.InstanceId.randomInstanceId;
import static com.namazustudios.socialengine.rt.id.NodeId.forInstanceAndApplication;
import static com.namazustudios.socialengine.rt.id.ResourceId.randomResourceIdForNode;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class TaskIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        assertNotNull(taskId.getNodeId());
        assertNotNull(taskId.getResourceId());
        assertNotNull(taskId.getNodeId().getInstanceId());
        assertNotNull(taskId.getNodeId().getApplicationId());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asBytes());
        assertEquals(duplicateTaskId, taskId);
        assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final NodeId nodeId = forInstanceAndApplication(instanceId, randomApplicationId());
        final ResourceId resourceId = randomResourceIdForNode(nodeId);
        final TaskId taskId = new TaskId(resourceId);
        final TaskId duplicateTaskId = new TaskId(taskId.asString());
        assertEquals(duplicateTaskId, taskId);
        assertEquals(duplicateTaskId.hashCode(), taskId.hashCode());
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
            assertEquals(result, taskId);
            assertEquals(result.hashCode(), taskId.hashCode());
        }

    }

}
