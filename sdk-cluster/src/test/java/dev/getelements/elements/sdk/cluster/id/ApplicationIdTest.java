package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.sdk.cluster.id.ApplicationId.randomApplicationId;

public class ApplicationIdTest {

    @Test
    public void testCreate() {
        final ApplicationId applicationId = randomApplicationId();
        Assert.assertNotNull(applicationId.getApplicationUUID());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final ApplicationId applicationId = randomApplicationId();
        final ApplicationId duplicateApplicationId = new ApplicationId(applicationId.asBytes());
        Assert.assertEquals(duplicateApplicationId, applicationId);
        Assert.assertEquals(duplicateApplicationId.hashCode(), applicationId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final ApplicationId applicationId = randomApplicationId();
        final ApplicationId duplicateApplicationId = new ApplicationId(applicationId.asString());
        Assert.assertEquals(duplicateApplicationId, applicationId);
        Assert.assertEquals(duplicateApplicationId.hashCode(), applicationId.hashCode());
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
            Assert.assertEquals(result, applicationId);
            Assert.assertEquals(result.hashCode(), applicationId.hashCode());
        }

    }

}
