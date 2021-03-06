package com.namazustudios.socialengine.rt;

import com.namazustudios.socialengine.rt.id.*;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static com.namazustudios.socialengine.rt.id.ApplicationId.randomApplicationId;
import static java.util.UUID.randomUUID;
import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

public class ApplicationIdTest {

    @Test
    public void testCreate() {
        final ApplicationId applicationId = randomApplicationId();
        assertNotNull(applicationId.getApplicationUUID());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final ApplicationId applicationId = randomApplicationId();
        final ApplicationId duplicateApplicationId = new ApplicationId(applicationId.asBytes());
        assertEquals(duplicateApplicationId, applicationId);
        assertEquals(duplicateApplicationId.hashCode(), applicationId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final ApplicationId applicationId = randomApplicationId();
        final ApplicationId duplicateApplicationId = new ApplicationId(applicationId.asString());
        assertEquals(duplicateApplicationId, applicationId);
        assertEquals(duplicateApplicationId.hashCode(), applicationId.hashCode());
    }

    @Test
    public void testSerialization() throws Exception {

        final ApplicationId applicationId = randomApplicationId();

        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {

            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(applicationId);
            }

            bytes = bos.toByteArray();

        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            assertEquals(result, applicationId);
            assertEquals(result.hashCode(), applicationId.hashCode());
        }

    }

}
