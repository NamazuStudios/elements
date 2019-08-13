package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.InstanceId;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class InstanceIdTest {

    @Test
    public void testCreate() {
        final InstanceId instanceId = new InstanceId();
        assertNotNull(instanceId.getUuid());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final InstanceId instanceId = new InstanceId();
        final InstanceId duplicateInstanceId = new InstanceId(instanceId.asBytes());
        assertEquals(duplicateInstanceId, instanceId);
        assertEquals(duplicateInstanceId.hashCode(), instanceId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final InstanceId instanceId = new InstanceId();
        final InstanceId duplicateInstanceId = new InstanceId(instanceId.asString());
        assertEquals(duplicateInstanceId, instanceId);
        assertEquals(duplicateInstanceId.hashCode(), instanceId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final InstanceId instanceId = new InstanceId();

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
            assertEquals(result, instanceId);
            assertEquals(result.hashCode(), instanceId.hashCode());
        }

    }

}
