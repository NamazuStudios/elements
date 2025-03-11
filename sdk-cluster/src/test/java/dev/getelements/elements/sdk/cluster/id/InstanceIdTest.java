package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.sdk.cluster.id.InstanceId.randomInstanceId;

public class InstanceIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = randomInstanceId();
        Assert.assertNotNull(instanceId.getUuid());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = randomInstanceId();
        final InstanceId duplicateInstanceId = new InstanceId(instanceId.asBytes());
        Assert.assertEquals(duplicateInstanceId, instanceId);
        Assert.assertEquals(duplicateInstanceId.hashCode(), instanceId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = randomInstanceId();
        final InstanceId duplicateInstanceId = new InstanceId(instanceId.asString());
        Assert.assertEquals(duplicateInstanceId, instanceId);
        Assert.assertEquals(duplicateInstanceId.hashCode(), instanceId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = randomInstanceId();

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(instanceId);
            }

            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            Assert.assertEquals(result, instanceId);
            Assert.assertEquals(result.hashCode(), instanceId.hashCode());
        }

    }

}
