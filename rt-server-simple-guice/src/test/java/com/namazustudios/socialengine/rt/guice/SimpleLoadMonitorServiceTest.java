package com.namazustudios.socialengine.rt.guice;

import com.google.inject.AbstractModule;
import com.google.inject.Inject;
import com.namazustudios.socialengine.rt.LoadMonitorService;
import org.testng.annotations.Guice;
import org.testng.annotations.Test;

import static org.testng.Assert.assertTrue;

@Guice(modules = SimpleLoadMonitorServiceTest.Module.class)
public class SimpleLoadMonitorServiceTest {

    private LoadMonitorService loadMonitorService;

    @Test
    public void testStartStop() {
        getLoadMonitorService().start();
        final double quality = getLoadMonitorService().getInstanceQuality();
        assertTrue(quality != Double.NaN);
        getLoadMonitorService().stop();
    }

    public LoadMonitorService getLoadMonitorService() {
        return loadMonitorService;
    }

    @Inject
    public void setLoadMonitorService(LoadMonitorService loadMonitorService) {
        this.loadMonitorService = loadMonitorService;
    }

    public static class Module extends AbstractModule {
        @Override
        protected void configure() {
            install(new SimpleLoadMonitorServiceModule());
        }
    }

}
