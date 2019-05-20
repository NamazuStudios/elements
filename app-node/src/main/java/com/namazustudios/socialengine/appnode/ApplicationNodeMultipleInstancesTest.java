package com.namazustudios.socialengine.appnode;

import com.namazustudios.socialengine.config.DefaultConfigurationSupplier;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testng.annotations.Test;

import java.util.ArrayList;
import java.util.List;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertTrue;

public class ApplicationNodeMultipleInstancesTest {

    private static final Logger logger = LoggerFactory.getLogger(ApplicationNodeMultipleInstancesTest.class);

    private final List<ApplicationNode> applicationNodes = new ArrayList<>();

    private final List<Thread> applicationNodeThreads = new ArrayList<>();

    private final int APPLICATION_NODE_COUNT = 5;

    @Test()
    public void testCreateApplicationNodes() {
        final DefaultConfigurationSupplier defaultConfigurationSupplier;
        defaultConfigurationSupplier = new DefaultConfigurationSupplier();

        for (int i=0; i<APPLICATION_NODE_COUNT; i++) {
            ApplicationNode applicationNode = new ApplicationNode(defaultConfigurationSupplier);
            applicationNodes.add(applicationNode);
        }

        assertEquals(applicationNodes.size(), APPLICATION_NODE_COUNT);
    }

    @Test(dependsOnMethods = {"testCreateApplicationNodes"})
    public void testStartApplicationNodes() {
        for (final ApplicationNode applicationNode : applicationNodes) {
            final Thread thread = new Thread(() -> {
                System.out.println(applicationNode);
                System.out.println(Thread.currentThread());
                applicationNode.start();
            });
            thread.start();
            applicationNodeThreads.add(thread);
        }

        assertEquals(applicationNodeThreads.size(), APPLICATION_NODE_COUNT);
    }

    @Test(dependsOnMethods = {"testStartApplicationNodes"})
    public void testDiscoveryRegistration() {

    }
}
