package dev.getelements.elements.sdk.cluster.id;

import org.testng.Assert;
import org.testng.annotations.Test;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

import static dev.getelements.elements.sdk.cluster.id.DeploymentId.randomDeploymentId;

public class DeploymentIdTest {

    @Test
    public void testCreate() {
        final DeploymentId deploymentId = randomDeploymentId();
        Assert.assertNotNull(deploymentId.getDeploymentUUID());
    }

    @Test
    public void testEqualsAndHashCodeWithBytes() {
        final DeploymentId deploymentId = randomDeploymentId();
        final DeploymentId duplicateDeploymentId = new DeploymentId(deploymentId.asBytes());
        Assert.assertEquals(duplicateDeploymentId, deploymentId);
        Assert.assertEquals(duplicateDeploymentId.hashCode(), deploymentId.hashCode());
    }

    @Test
    public void testEqualsAndHashCodeWithString() {
        final DeploymentId deploymentId = randomDeploymentId();
        final DeploymentId duplicateDeploymentId = new DeploymentId(deploymentId.asString());
        Assert.assertEquals(duplicateDeploymentId, deploymentId);
        Assert.assertEquals(duplicateDeploymentId.hashCode(), deploymentId.hashCode());
    }

    @Test
    public void testValueOfRoundTrips() {
        final DeploymentId deploymentId = randomDeploymentId();
        Assert.assertEquals(DeploymentId.valueOf(deploymentId.asString()), deploymentId);
    }

    @Test
    public void testNullDeploymentIdConstant() {
        Assert.assertEquals(DeploymentId.NULL_DEPLOYMENT_ID.getDeploymentUUID().getMostSignificantBits(), 0L);
        Assert.assertEquals(DeploymentId.NULL_DEPLOYMENT_ID.getDeploymentUUID().getLeastSignificantBits(), 0L);
    }

    @Test
    public void testSerialization() throws Exception {

        final DeploymentId deploymentId = randomDeploymentId();
        final byte[] bytes;

        try (final ByteArrayOutputStream bos = new ByteArrayOutputStream()) {
            try (final ObjectOutputStream oos = new ObjectOutputStream(bos)) {
                oos.writeObject(deploymentId);
            }
            bytes = bos.toByteArray();
        }

        try (final ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
             final ObjectInputStream ois = new ObjectInputStream(bis)) {
            final Object result = ois.readObject();
            Assert.assertEquals(result, deploymentId);
            Assert.assertEquals(result.hashCode(), deploymentId.hashCode());
        }

    }

}
